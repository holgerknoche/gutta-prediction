package gutta.prediction.span;

import gutta.prediction.analysis.consistency.ConsistencyIssue;
import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ImplicitTransactionAbortEvent;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import gutta.prediction.simulation.TraceSimulationContext;
import gutta.prediction.simulation.TraceSimulationListener;
import gutta.prediction.simulation.TraceSimulationMode;
import gutta.prediction.simulation.Transaction;
import gutta.prediction.span.EntityEvent.EntityAccessType;
import gutta.prediction.span.TransactionEvent.TransactionEventType;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static gutta.prediction.simulation.TraceSimulator.runSimulationOf;

/**
 * Worker class for the {@link TraceBuilder} to actually build the trace.
 */
class TraceBuilderWorker implements TraceSimulationListener {

    private final Deque<SpanState> stack = new ArrayDeque<>();

    private final Map<Transaction, SpanState> pendingSuspendedStates = new HashMap<>();

    private Map<MonitoringEvent, Set<ConsistencyIssue<?>>> eventToIssues;

    private long traceId;

    private String traceName;

    private Span rootSpan;

    private Span currentSpan;

    private TransactionOverlay currentTransactionOverlay;

    public Trace buildTrace(EventTrace eventTrace, DeploymentModel deploymentModel, Set<ConsistencyIssue<?>> consistencyIssues) {
        this.eventToIssues = createIssueLookup(consistencyIssues);

        runSimulationOf(eventTrace, deploymentModel, TraceSimulationMode.WITH_TRANSACTIONS, this);

        return new Trace(this.traceId, this.traceName, this.rootSpan);
    }

    private static Map<MonitoringEvent, Set<ConsistencyIssue<?>>> createIssueLookup(Set<ConsistencyIssue<?>> consistencyIssues) {
        if (consistencyIssues == null) {
            return Map.of();
        }

        var eventToIssues = new HashMap<MonitoringEvent, Set<ConsistencyIssue<?>>>();
        for (var issue : consistencyIssues) {
            var issuesForEvent = eventToIssues.computeIfAbsent(issue.event(), event -> new HashSet<>());
            issuesForEvent.add(issue);
        }

        return eventToIssues;
    }

    @Override
    public void onUseCaseStartEvent(UseCaseStartEvent event, TraceSimulationContext context) {
        this.traceId = event.traceId();
        this.traceName = event.name();

        var newSpan = new Span(context.currentComponent().name(), event.timestamp(), null);

        this.rootSpan = newSpan;
        this.currentSpan = newSpan;
    }

    private static boolean locationChange(MonitoringEvent firstEvent, MonitoringEvent secondEvent) {
        return !(firstEvent.location().equals(secondEvent.location()));
    }

    @Override
    public void beforeComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent,
            ComponentConnection connection, TraceSimulationContext context) {

        if (locationChange(invocationEvent, entryEvent)) {
            // Save the current state to the stack
            var newState = new SpanState(this.currentSpan, this.currentTransactionOverlay);
            this.stack.push(newState);

            // Build the new state
            var currentTimestamp = entryEvent.timestamp();
            var spanName = connection.target().name();
            this.currentSpan = new Span(spanName, currentTimestamp, this.currentSpan);
            this.currentTransactionOverlay = null;
        }

        this.addOverheadOverlayIfNecessary(invocationEvent, entryEvent);
    }

    private void addOverheadOverlayIfNecessary(MonitoringEvent startEvent, MonitoringEvent endEvent) {
        var overhead = (endEvent.timestamp() - startEvent.timestamp());

        if (overhead > 0) {
            var overheadOverlay = new OverheadOverlay(startEvent.timestamp(), endEvent.timestamp());
            this.currentSpan.addOverlay(overheadOverlay);
        }
    }

    @Override
    public void beforeComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection,
            TraceSimulationContext context) {

        this.addOverheadOverlayIfNecessary(exitEvent, returnEvent);

        if (locationChange(exitEvent, returnEvent)) {
            // Adjust the end timestamp of the current span
            this.currentSpan.endTimestamp(exitEvent.timestamp());

            var currentTransaction = context.currentTransaction();
            if (currentTransaction != null && currentTransaction.isSubordinate()) {
                var currentTimestamp = exitEvent.timestamp();

                // If a subordinate transaction is active, we need to end the current overlay (there should be one) and add a suspension overlay
                this.currentTransactionOverlay.endTimestamp(currentTimestamp);
                var newOverlay = new SuspendedTransactionOverlay(currentTimestamp, this.currentTransactionOverlay.isDirty());

                this.currentSpan.addOverlay(newOverlay);

                var pendingState = new SpanState(this.currentSpan, newOverlay);
                this.pendingSuspendedStates.put(currentTransaction, pendingState);
            }
        }
    }

    @Override
    public void afterComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection,
            TraceSimulationContext context) {

        if (locationChange(exitEvent, returnEvent)) {
            // Restore the state from the stack. We do this after the component return so that possible end-of-transaction events are
            // added to the right span
            var newState = this.stack.pop();
            this.currentSpan = newState.span();
            this.currentTransactionOverlay = newState.transactionOverlay();
        }
    }

    @Override
    public void onTransactionStart(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        var newOverlay = new CleanTransactionOverlay(event.timestamp());

        this.currentSpan.addOverlay(newOverlay);
        this.currentTransactionOverlay = newOverlay;

        this.currentSpan.addEvent(new TransactionEvent(event.timestamp(), TransactionEventType.START));
    }

    @Override
    public void onTransactionCommit(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        this.handleTransactionCompletion(event, transaction, TransactionEventType.COMMIT);
    }

    private void handleTransactionCompletion(MonitoringEvent event, Transaction transaction, TransactionEventType eventType) {
        var currentTimestamp = event.timestamp();

        var pendingState = this.pendingSuspendedStates.remove(transaction);
        TransactionOverlay affectedOverlay;
        Span affectedSpan;

        if (pendingState == null) {
            affectedOverlay = this.currentTransactionOverlay;
            affectedSpan = this.currentSpan;
        } else {
            affectedOverlay = pendingState.transactionOverlay();
            affectedSpan = pendingState.span();
        }

        if (affectedOverlay != null) {
            affectedOverlay.endTimestamp(currentTimestamp);
        }

        affectedSpan.addEvent(new TransactionEvent(currentTimestamp, eventType));
    }

    @Override
    public void onImplicitTransactionAbortEvent(ImplicitTransactionAbortEvent event, TraceSimulationContext context) {
        this.currentSpan.addEvent(new TransactionEvent(event.timestamp(), TransactionEventType.IMPLICIT_ABORT, event.cause()));
    }

    @Override
    public void onTransactionAbort(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        this.handleTransactionCompletion(event, transaction, TransactionEventType.EXPLICIT_ABORT);
    }

    @Override
    public void onEntityReadEvent(EntityReadEvent event, TraceSimulationContext context) {
        var issues = this.eventToIssues.get(event);

        if (issues != null) {
            issues.forEach(issue -> this.currentSpan.addEvent(new ConsistencyIssueEvent(issue)));
        } else {
            this.currentSpan.addEvent(new EntityEvent(event.timestamp(), EntityAccessType.READ, event.entity()));
        }
    }

    @Override
    public void onEntityWriteEvent(EntityWriteEvent event, TraceSimulationContext context) {
        var issues = this.eventToIssues.get(event);

        if (issues != null) {
            issues.forEach(issue -> this.currentSpan.addEvent(new ConsistencyIssueEvent(issue)));
        } else {
            this.currentSpan.addEvent(new EntityEvent(event.timestamp(), EntityAccessType.WRITE, event.entity()));
        }

        if (this.currentTransactionOverlay == null || this.currentTransactionOverlay.isDirty()) {
            // If there is no transaction overlay or it is already marked as dirty, nothing needs to be done
            return;
        }

        // End the current overlay and add a new one marked as "dirty"
        var currentTimestamp = event.timestamp();
        this.currentTransactionOverlay.endTimestamp(currentTimestamp);

        var newOverlay = new DirtyTransactionOverlay(currentTimestamp);
        this.currentSpan.addOverlay(newOverlay);
        this.currentTransactionOverlay = newOverlay;
    }

    @Override
    public void onTransactionSuspend(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        if (this.currentTransactionOverlay == null) {
            return;
        }

        var currentTimestamp = event.timestamp();
        this.currentTransactionOverlay.endTimestamp(currentTimestamp);

        var newOverlay = new SuspendedTransactionOverlay(currentTimestamp, this.currentTransactionOverlay.isDirty());

        this.currentSpan.addOverlay(newOverlay);
        this.currentTransactionOverlay = newOverlay;
    }

    @Override
    public void onTransactionResume(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        if (this.currentTransactionOverlay == null) {
            return;
        }

        var currentTimestamp = event.timestamp();
        this.currentTransactionOverlay.endTimestamp(currentTimestamp);

        var newOverlay = (this.currentTransactionOverlay.isDirty()) ? new DirtyTransactionOverlay(currentTimestamp)
                : new CleanTransactionOverlay(currentTimestamp);

        this.currentSpan.addOverlay(newOverlay);
        this.currentTransactionOverlay = newOverlay;
    }

    @Override
    public void onUseCaseEndEvent(UseCaseEndEvent event, TraceSimulationContext context) {
        this.rootSpan.endTimestamp(event.timestamp());
    }

    private record SpanState(Span span, TransactionOverlay transactionOverlay) {
    }

}
