package gutta.prediction.stream;

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

public interface EventStreamProcessorListener {

    default void onStartOfProcessing() {
        // Do nothing by default
    }
    
    default void onEndOfProcessing() {
        // Do nothing by default
    }
    
    default void onEntityReadEvent(EntityReadEvent event, EventProcessingContext context) {
        // Do nothing by default
    }
    
    default void onEntityWriteEvent(EntityWriteEvent event, EventProcessingContext context) {
        // Do nothing by default
    }
    
    default void onServiceCandidateEntryEvent(ServiceCandidateEntryEvent event, EventProcessingContext context) {
        // Do nothing by default
    }
    
    default void onServiceCandidateExitEvent(ServiceCandidateExitEvent event, EventProcessingContext context) {
        // Do nothing by default
    }
    
    default void onComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection, EventProcessingContext context) {
        // Do nothing by default
    }
    
    default void onServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event, EventProcessingContext context) {
        // Do nothing by default
    }
    
    default void onComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ComponentConnection connection, EventProcessingContext context) {
        // Do nothing by default
    }
    
    default void onServiceCandidateReturnEvent(ServiceCandidateReturnEvent event, EventProcessingContext context) {
        // Do nothing by default
    }
    
    default void onTransactionAbortEvent(TransactionAbortEvent event, EventProcessingContext context) {
        // Do nothing by default
    }
    
    default void onTransactionCommitEvent(TransactionCommitEvent event, EventProcessingContext context) {
        // Do nothing by default
    }
    
    default void onTransactionStartEvent(TransactionStartEvent event, EventProcessingContext context) {
        // Do nothing by default        
    }
    
    default void onUseCaseStartEvent(UseCaseStartEvent event, EventProcessingContext context) {
        // Do nothing by default
    }
    
    default void onUseCaseEndEvent(UseCaseEndEvent event, EventProcessingContext context) {
        // Do nothing by default        
    }
    
}
