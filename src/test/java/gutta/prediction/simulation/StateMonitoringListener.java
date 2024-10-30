package gutta.prediction.simulation;

import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.ExplicitTransactionAbortEvent;
import gutta.prediction.event.ImplicitTransactionAbortEvent;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;

import java.util.ArrayList;
import java.util.List;

class StateMonitoringListener implements TraceSimulationListener {
    
    private final List<SimulationState> assumedStates = new ArrayList<>();
    
    @Override
    public void onEntityReadEvent(EntityReadEvent event, TraceSimulationContext context) {
        this.recordState(event, context);
    }
    
    @Override
    public void onEntityWriteEvent(EntityWriteEvent event, TraceSimulationContext context) {
        this.recordState(event, context);
    }
    
    @Override
    public void onServiceCandidateEntryEvent(ServiceCandidateEntryEvent event, TraceSimulationContext context) {
        this.recordState(event, context);
    }
    
    @Override
    public void onServiceCandidateExitEvent(ServiceCandidateExitEvent event, TraceSimulationContext context) {
        this.recordState(event, context);
    }
    
    @Override
    public void onServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event, TraceSimulationContext context) {
        this.recordState(event, context);
    }
    
    @Override
    public void onServiceCandidateReturnEvent(ServiceCandidateReturnEvent event, TraceSimulationContext context) {
        this.recordState(event, context);
    }
    
    @Override
    public void onImplicitTransactionAbortEvent(ImplicitTransactionAbortEvent event, TraceSimulationContext context) {
        this.recordState(event, context);
    }
    
    @Override
    public void onExplicitTransactionAbortEvent(ExplicitTransactionAbortEvent event, TraceSimulationContext context) {
        this.recordState(event, context);
    }
    
    @Override
    public void onTransactionCommitEvent(TransactionCommitEvent event, TraceSimulationContext context) {
        this.recordState(event, context);
    }
    
    @Override
    public void onTransactionStartEvent(TransactionStartEvent event, TraceSimulationContext context) {
        this.recordState(event, context);
    }
    
    @Override
    public void onUseCaseEndEvent(UseCaseEndEvent event, TraceSimulationContext context) {
        this.recordState(event, context);
    }
    
    @Override
    public void onUseCaseStartEvent(UseCaseStartEvent event, TraceSimulationContext context) {
        this.recordState(event, context);
    }
    
    private void recordState(MonitoringEvent event, TraceSimulationContext context) {
        var state = new SimulationState(event, context.currentServiceCandidate(), context.currentComponent(), context.currentLocation(), context.currentTransaction());
        this.assumedStates.add(state);
    }
    
    public List<SimulationState> assumedStates() {
        return this.assumedStates;
    }
    
}
