package gutta.prediction.span;

import gutta.prediction.analysis.consistency.ConsistencyIssue;
import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import gutta.prediction.simulation.TraceSimulationContext;
import gutta.prediction.simulation.TraceSimulationListener;
import gutta.prediction.simulation.Transaction;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static gutta.prediction.simulation.TraceSimulator.runSimulationOf;

class TraceBuilderWorker implements TraceSimulationListener {

    private final Deque<SpanState> stack = new ArrayDeque<>();
    
    private final Map<Transaction, SuspendedTransactionOverlay> pendingSuspendedOverlays = new HashMap<>();
       
    private long traceId;
    
    private String traceName;
    
    private Span rootSpan;
    
    private Span currentSpan;
    
    private TransactionOverlay currentTransactionOverlay;
    
    public Trace buildTrace(EventTrace eventTrace, DeploymentModel deploymentModel, Set<ConsistencyIssue<?>> consistencyIssues) {
        runSimulationOf(eventTrace, deploymentModel, this);

        return new Trace(this.traceId, this.traceName, this.rootSpan);
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
    public void onComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ComponentConnection connection,
            TraceSimulationContext context) {

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
        
        this.addLatencyOverlayIfNecessary(invocationEvent, entryEvent);
    }
                
    private void addLatencyOverlayIfNecessary(MonitoringEvent startEvent, MonitoringEvent endEvent) {
        var latency = (endEvent.timestamp() - startEvent.timestamp());
        
        if (latency > 0) {
            var latencyOverlay = new LatencyOverlay(startEvent.timestamp(), endEvent.timestamp());
            this.currentSpan.addOverlay(latencyOverlay);
        }
    }

    @Override
    public void onComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection,
            TraceSimulationContext context) {

        this.addLatencyOverlayIfNecessary(exitEvent, returnEvent);
        
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
                this.pendingSuspendedOverlays.put(currentTransaction, newOverlay);
            }
            
            // Restore the state from the stack
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
    }
    
    @Override
    public void onTransactionCommit(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        // TODO Add successful commit marker
        this.completeTransactionOverlay(event, transaction);
    }
    
    private void completeTransactionOverlay(MonitoringEvent event, Transaction transaction) {
        var currentTimestamp = event.timestamp();
        
        if (this.currentTransactionOverlay != null) {
            // If an overlay is active, adjust the end timestamp and remove it so it is not touched again
            this.currentTransactionOverlay.endTimestamp(currentTimestamp);
            this.currentTransactionOverlay = null;
        }

        // If a suspended overlay is attached to the transaction, complete it 
        var suspendedOverlay = this.pendingSuspendedOverlays.remove(transaction);
        if (suspendedOverlay != null) {
            suspendedOverlay.endTimestamp(event.timestamp());
        }
    }
        
    @Override
    public void onTransactionAbort(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        // TODO Add abort marker
        this.completeTransactionOverlay(event, transaction);
    }
    
    @Override
    public void onEntityWriteEvent(EntityWriteEvent event, TraceSimulationContext context) {
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
    public void onUseCaseEndEvent(UseCaseEndEvent event, TraceSimulationContext context) {
        this.rootSpan.endTimestamp(event.timestamp());
    }
    
    private record SpanState(Span span, TransactionOverlay transactionOverlay) {}

}
