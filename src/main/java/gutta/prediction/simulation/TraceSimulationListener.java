package gutta.prediction.simulation;

import gutta.prediction.domain.ComponentConnection;
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

/**
 * Listener interface to receive events of a trace simulation.
 */
public interface TraceSimulationListener {

    /**
     * This method is invoked before the first event in the trace is processed. 
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void onStartOfProcessing() {
        // Do nothing by default
    }

    /**
     * This method is invoked after the last event in the trace is processed.
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void onEndOfProcessing() {
        // Do nothing by default
    }

    /**
     * This method is invoked if a {@link EntityReadEvent} is encountered in the trace. 
     * 
     * @param event The encountered event
     * @param context The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void onEntityReadEvent(EntityReadEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a read-write conflict is detected when processing an {@link EntityReadEvent}.
     * The invocation occurs after that of {@link #onEntityReadEvent(EntityReadEvent, TraceSimulationContext)} for the respective event.
     * 
     * @param event The event at which the conflict occurred
     * @param context The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.WITH_ENTITY_ACCESSES)
    default void onReadWriteConflict(EntityReadEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }
    
    /**
     * This method is invoked if a {@link EntityWriteEvent} is encountered in the trace. 
     * 
     * @param event The encountered event
     * @param context The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void onEntityWriteEvent(EntityWriteEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }
    
    /**
     * This method is invoked if a write-write conflict is detected when processing an {@link EntityWriteEvent}.
     * The invocation occurs after that of {@link #onEntityWriteEvent(EntityReadEvent, TraceSimulationContext)} for the respective event.
     * 
     * @param event The event at which the conflict occurred
     * @param context The current simulation context
     */
    default void onWriteWriteConflict(EntityWriteEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }
    
    default void onCommittedWrite(EntityWriteEvent event, TraceSimulationContext context) {
        // Do nothing by default        
    }
    
    default void onRevertedWrite(EntityWriteEvent event, TraceSimulationContext context) {
     // Do nothing by default
    }

    default void onServiceCandidateEntryEvent(ServiceCandidateEntryEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    default void onServiceCandidateExitEvent(ServiceCandidateExitEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    default void beforeComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection,
            TraceSimulationContext context) {
        // Do nothing by default
    }
    
    default void afterComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection,
            TraceSimulationContext context) {
     // Do nothing by default
    }

    default void onServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    default void beforeComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ComponentConnection connection,
            TraceSimulationContext context) {
        // Do nothing by default
    }
    
    default void afterComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ComponentConnection connection,
            TraceSimulationContext context) {
        // Do nothing by default
    }

    default void onTransactionStart(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        // Do nothing by default
    }
    
    default void onTransactionSuspend(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        // Do nothing by default
    }
    
    default void onTransactionResume(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        // Do nothing by default
    }

    default void onTransactionCommit(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        // Do nothing by default
    }

    default void onTransactionAbort(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        // Do nothing by default
    }

    default void onServiceCandidateReturnEvent(ServiceCandidateReturnEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    default void onImplicitTransactionAbortEvent(ImplicitTransactionAbortEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    default void onExplicitTransactionAbortEvent(ExplicitTransactionAbortEvent event, TraceSimulationContext context) {
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
