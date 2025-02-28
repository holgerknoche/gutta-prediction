package gutta.prediction.span;

import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ExplicitTransactionAbortEvent;
import gutta.prediction.event.ImplicitTransactionAbortEvent;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.MonitoringEventVisitor;
import gutta.prediction.event.ObservedLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.SyntheticLocation;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import gutta.prediction.simulation.EventStream;
import gutta.prediction.span.EntityEvent.EntityAccessType;
import gutta.prediction.span.TransactionEvent.TransactionEventType;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * An {@link ObservedTraceBuilder} builds a span trace from an observed event trace to provide a quick overview. No transaction simulation is performed, and
 * therefore, no overlays are shown.
 */
public class ObservedTraceBuilder extends MonitoringEventVisitor {

    private final EventStream events;

    private final Deque<Span> stack;

    private long traceId;

    private String traceName;

    private Span rootSpan;

    private Span currentSpan;

    /**
     * Creates a new builder for the given event trace.
     * 
     * @param trace The event trace to convert into a span trace
     */
    public ObservedTraceBuilder(EventTrace trace) {
        this.events = new EventStream(trace);
        this.stack = new ArrayDeque<>();
    }

    /**
     * Builds the span trace from the event trace.
     * 
     * @return The built span trace
     */
    public Trace buildTrace() {
        this.events.forEachRemaining(this::handleMonitoringEvent);

        return new Trace(this.traceId, this.traceName, this.rootSpan);
    }

    @Override
    protected void handleUseCaseStartEvent(UseCaseStartEvent event) {
        this.traceId = event.traceId();
        this.traceName = event.name();

        var newSpan = new Span(formatLocation(event.location(), event.name()), event.timestamp());

        this.rootSpan = newSpan;
        this.currentSpan = newSpan;
        this.stack.push(this.rootSpan);
    }

    @Override
    protected void handleUseCaseEndEvent(UseCaseEndEvent event) {
        this.rootSpan.endTimestamp(event.timestamp());
    }

    @Override
    protected void handleTransactionStartEvent(TransactionStartEvent event) {
        this.currentSpan.addEvent(new TransactionEvent(event.timestamp(), TransactionEventType.START));
    }

    @Override
    protected void handleTransactionCommitEvent(TransactionCommitEvent event) {
        this.currentSpan.addEvent(new TransactionEvent(event.timestamp(), TransactionEventType.COMMIT));
    }

    @Override
    protected void handleExplicitTransactionAbortEvent(ExplicitTransactionAbortEvent event) {
        this.currentSpan.addEvent(new TransactionEvent(event.timestamp(), TransactionEventType.EXPLICIT_ABORT));
    }

    @Override
    protected void handleImplicitTransactionAbortEvent(ImplicitTransactionAbortEvent event) {
        this.currentSpan.addEvent(new TransactionEvent(event.timestamp(), TransactionEventType.IMPLICIT_ABORT));
    }

    @Override
    protected void handleServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event) {
        var nextEvent = this.events.lookahead(1);

        if (nextEvent instanceof ServiceCandidateEntryEvent entryEvent) {
            if (locationChange(event, entryEvent)) {
                var newSpan = new Span(formatLocation(entryEvent.location(), entryEvent.name()), entryEvent.timestamp(), this.currentSpan);
                this.stack.push(this.currentSpan);
                this.currentSpan = newSpan;

                var overhead = calculateOverhead(event, entryEvent);
                if (overhead > 0) {
                    newSpan.addOverlay(new OverheadOverlay(event.timestamp(), entryEvent.timestamp()));
                }
            }
        } else {
            throw new IllegalStateException("Candidate invocation event '" + event + "' is not followed by a candidate entry event.");
        }
    }

    private static long calculateOverhead(MonitoringEvent startEvent, MonitoringEvent endEvent) {
        return (endEvent.timestamp() - startEvent.timestamp());
    }

    private static boolean locationChange(MonitoringEvent event1, MonitoringEvent event2) {
        return !(event1.location().equals(event2.location()));
    }

    @Override
    protected void handleServiceCandidateExitEvent(ServiceCandidateExitEvent event) {
        var nextEvent = this.events.lookahead(1);

        if (nextEvent instanceof ServiceCandidateReturnEvent returnEvent) {
            if (locationChange(event, returnEvent)) {
                this.currentSpan.endTimestamp(event.timestamp());

                var overhead = calculateOverhead(event, returnEvent);
                if (overhead > 0) {
                    this.currentSpan.addOverlay(new OverheadOverlay(event.timestamp(), returnEvent.timestamp()));
                }

                var span = this.stack.pop();
                this.currentSpan = span;
            }
        } else {
            throw new IllegalStateException("Candidate exit event '" + event + "' is not followed by a candidate return event.");
        }
    }

    @Override
    protected void handleEntityReadEvent(EntityReadEvent event) {
        this.currentSpan.addEvent(new EntityEvent(event.timestamp(), EntityAccessType.READ, event.entity()));
    }

    @Override
    protected void handleEntityWriteEvent(EntityWriteEvent event) {
        this.currentSpan.addEvent(new EntityEvent(event.timestamp(), EntityAccessType.WRITE, event.entity()));
    }

    private static String formatLocation(Location location, String name) {
        return switch (location) {
        case ObservedLocation observed -> observed.hostname() + ":" + observed.processId() + ":" + observed.threadId() + " (" + name + ")";
        case SyntheticLocation synthetic -> "*" + synthetic.id() + " (" + name + ")";
        };
    }

}
