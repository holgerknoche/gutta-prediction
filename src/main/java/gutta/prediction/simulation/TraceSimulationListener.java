package gutta.prediction.simulation;

import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionAbortEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;

public interface TraceSimulationListener {

    default void onStartOfProcessing() {
        // Do nothing by default
    }
    
    default void onEndOfProcessing() {
        // Do nothing by default
    }
    
    default void onEntityReadEvent(EntityReadEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }
    
    default void onEntityWriteEvent(EntityWriteEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }
    
    default void onServiceCandidateEntryEvent(ServiceCandidateEntryEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }
    
    default void onServiceCandidateExitEvent(ServiceCandidateExitEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }
    
    default void onComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection, TraceSimulationContext context) {
        // Do nothing by default
    }
    
    default void onServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }
    
    default void onComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ComponentConnection connection, TraceSimulationContext context) {
        // Do nothing by default
    }
        
    default void onServiceCandidateReturnEvent(ServiceCandidateReturnEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }
    
    default void onTransactionAbortEvent(TransactionAbortEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }
    
    default void onTransactionCommitEvent(TransactionCommitEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }
    
    default void onTransactionStartEvent(TransactionStartEvent event, TraceSimulationContext context) {
        // Do nothing by default        
    }
    
    default void onUseCaseStartEvent(UseCaseStartEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }
    
    default void onUseCaseEndEvent(UseCaseEndEvent event, TraceSimulationContext context) {
        // Do nothing by default        
    }
    
}
