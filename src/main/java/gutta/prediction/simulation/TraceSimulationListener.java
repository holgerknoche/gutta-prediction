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
     * @param event   The encountered event
     * @param context The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void onEntityReadEvent(EntityReadEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a read-write conflict is detected when processing an {@link EntityReadEvent}. The invocation occurs after that of
     * {@link #onEntityReadEvent(EntityReadEvent, TraceSimulationContext)} for the respective event.
     * 
     * @param event   The event at which the conflict occurred
     * @param context The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.WITH_ENTITY_ACCESSES)
    default void onReadWriteConflict(EntityReadEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a {@link EntityWriteEvent} is encountered in the trace.
     * 
     * @param event   The encountered event
     * @param context The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void onEntityWriteEvent(EntityWriteEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a write-write conflict is detected when processing an {@link EntityWriteEvent}. The invocation occurs after that of
     * {@link #onEntityWriteEvent(EntityReadEvent, TraceSimulationContext)} for the respective event.
     * 
     * @param event   The event at which the conflict occurred
     * @param context The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.WITH_ENTITY_ACCESSES)
    default void onWriteWriteConflict(EntityWriteEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a write is committed at the end of a transaction.
     * 
     * @param event   The entity write event representing the write
     * @param context The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.WITH_ENTITY_ACCESSES)
    default void onCommittedWrite(EntityWriteEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a write is reverted at the end of a transaction.
     * 
     * @param event   The entity write event representing the write
     * @param context The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.WITH_ENTITY_ACCESSES)
    default void onRevertedWrite(EntityWriteEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a {@link ServiceCandidateEntryEvent} is encountered in the trace.
     * 
     * @param event   The encountered event
     * @param context The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void onServiceCandidateEntryEvent(ServiceCandidateEntryEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a {@link ServiceCandidateExitEvent} is encountered in the trace.
     * 
     * @param event   The encountered event
     * @param context The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void onServiceCandidateExitEvent(ServiceCandidateExitEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked immediately before the return from a component invocation, i.e., the current state is still that of the invoked component. The
     * invocation occurs after that of {@link #onServiceCandidateExitEvent(ServiceCandidateExitEvent, TraceSimulationContext)}, but before that of
     * {@link #onServiceCandidateReturnEvent(ServiceCandidateReturnEvent, TraceSimulationContext)} for the respective events.
     * 
     * @param exitEvent   The exit event of the invoked candidate
     * @param returnEvent The return event of the invoked candidate
     * @param connection  The connection used for the return
     * @param context     The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void beforeComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection,
            TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked immediately after the return from a component invocation, i.e., the current state is already that of the invoking component. The
     * invocation occurs after that of {@link #onServiceCandidateExitEvent(ServiceCandidateExitEvent, TraceSimulationContext)}, but before that of
     * {@link #onServiceCandidateReturnEvent(ServiceCandidateReturnEvent, TraceSimulationContext)} for the respective events.
     * 
     * @param exitEvent   The exit event of the invoked candidate
     * @param returnEvent The return event of the invoked candidate
     * @param connection  The connection used for the return
     * @param context     The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void afterComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection,
            TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a {@link ServiceCandidateInvocationEvent} is encountered in the trace.
     * 
     * @param event   The encountered event
     * @param context The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void onServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked immediately before the transition to a component, i.e., the current state is still that of the invoking component. The invocation
     * occurs after that of {@link #onServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent, TraceSimulationContext)}, but before that of
     * {@link #onServiceCandidateEntryEvent(ServiceCandidateEntryEvent, TraceSimulationContext)} for the respective events.
     * 
     * @param invocationEvent The invocation event of the invoked candidate
     * @param entryEvent      The entry event of the invoked candidate
     * @param connection      The connection used for the transition
     * @param context         The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void beforeComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent,
            ComponentConnection connection, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked immediately after the transition to a component, i.e., the current state is already that of the invoked component. The invocation
     * occurs after that of {@link #onServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent, TraceSimulationContext)}, but before that of
     * {@link #onServiceCandidateEntryEvent(ServiceCandidateEntryEvent, TraceSimulationContext)} for the respective events.
     * 
     * @param invocationEvent The invocation event of the invoked candidate
     * @param entryEvent      The entry event of the invoked candidate
     * @param connection      The connection used for the transition
     * @param context         The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void afterComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent,
            ComponentConnection connection, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a transaction is started in the simulation.
     * 
     * @param event       The event at which the transaction was started
     * @param transaction The started transaction
     * @param context     The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.WITH_TRANSACTIONS)
    default void onTransactionStart(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a transaction is suspended in the simulation.
     * 
     * @param event       The event at which the transaction was suspended
     * @param transaction The suspended transaction
     * @param context     The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.WITH_TRANSACTIONS)
    default void onTransactionSuspend(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a suspended transaction is resumed in the simulation.
     * 
     * @param event       The event at which the transaction was resumed
     * @param transaction The suspended transaction
     * @param context     The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.WITH_TRANSACTIONS)
    default void onTransactionResume(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a suspended transaction is committed in the simulation.
     * 
     * @param event       The event at which the transaction was committed
     * @param transaction The suspended transaction
     * @param context     The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.WITH_TRANSACTIONS)
    default void onTransactionCommit(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a suspended transaction is aborted in the simulation.
     * 
     * @param event       The event at which the transaction was aborted
     * @param transaction The suspended transaction
     * @param context     The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.WITH_TRANSACTIONS)
    default void onTransactionAbort(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a {@link ServiceCandidateReturnEvent} is encountered in the trace.
     * 
     * @param event   The encountered event
     * @param context The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void onServiceCandidateReturnEvent(ServiceCandidateReturnEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a {@link ImplicitTransactionAbortEvent} is encountered in the trace.
     * 
     * @param event   The encountered event
     * @param context The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void onImplicitTransactionAbortEvent(ImplicitTransactionAbortEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a {@link ExplicitTransactionAbortEvent} is encountered in the trace.
     * 
     * @param event   The encountered event
     * @param context The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void onExplicitTransactionAbortEvent(ExplicitTransactionAbortEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a {@link TransactionCommitEvent} is encountered in the trace.
     * 
     * @param event   The encountered event
     * @param context The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void onTransactionCommitEvent(TransactionCommitEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a {@link TransactionStartEvent} is encountered in the trace.
     * 
     * @param event   The encountered event
     * @param context The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void onTransactionStartEvent(TransactionStartEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a {@link UseCaseStartEvent} is encountered in the trace.
     * 
     * @param event   The encountered event
     * @param context The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void onUseCaseStartEvent(UseCaseStartEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

    /**
     * This method is invoked if a {@link UseCaseEndEvent} is encountered in the trace.
     * 
     * @param event   The encountered event
     * @param context The current simulation context
     */
    @RequiredSimulationMode(TraceSimulationMode.BASIC)
    default void onUseCaseEndEvent(UseCaseEndEvent event, TraceSimulationContext context) {
        // Do nothing by default
    }

}
