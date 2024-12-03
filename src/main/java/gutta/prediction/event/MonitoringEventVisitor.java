package gutta.prediction.event;

/**
 * Abstract superclass for visitor implementations for monitoring events.
 * 
 * <p/>
 * <b>Implementation note:</b> This visitor was built as an abstract class instead of an interface so that the individual handle methods could be
 * {@code protected}.
 */
public class MonitoringEventVisitor {

    /**
     * Handles the given monitoring event by dispatching to the appropriate handle method.
     * 
     * @param event The event to handle
     */
    protected final void handleMonitoringEvent(MonitoringEvent event) {
        // Dispatch to the specific handler methods
        switch (event) {
        case EntityReadEvent specificEvent -> this.handleEntityReadEvent(specificEvent);
        case EntityWriteEvent specificEvent -> this.handleEntityWriteEvent(specificEvent);
        case ExplicitTransactionAbortEvent specificEvent -> this.handleExplicitTransactionAbortEvent(specificEvent);
        case ImplicitTransactionAbortEvent specificEvent -> this.handleImplicitTransactionAbortEvent(specificEvent);
        case ServiceCandidateEntryEvent specificEvent -> this.handleServiceCandidateEntryEvent(specificEvent);
        case ServiceCandidateExitEvent specificEvent -> this.handleServiceCandidateExitEvent(specificEvent);
        case ServiceCandidateInvocationEvent specificEvent -> this.handleServiceCandidateInvocationEvent(specificEvent);
        case ServiceCandidateReturnEvent specificEvent -> this.handleServiceCandidateReturnEvent(specificEvent);
        case TransactionCommitEvent specificEvent -> this.handleTransactionCommitEvent(specificEvent);
        case TransactionStartEvent specificEvent -> this.handleTransactionStartEvent(specificEvent);
        case UseCaseEndEvent specificEvent -> this.handleUseCaseEndEvent(specificEvent);
        case UseCaseStartEvent specificEvent -> this.handleUseCaseStartEvent(specificEvent);
        }
    }

    /**
     * Handles the given entity read event.
     * 
     * @param event The event to handle
     */
    protected void handleEntityReadEvent(EntityReadEvent event) {
        // Do nothing by default
    }

    /**
     * Handles the given entity write event.
     * 
     * @param event The event to handle
     */
    protected void handleEntityWriteEvent(EntityWriteEvent event) {
        // Do nothing by default
    }

    /**
     * Handles the given service candidate invocation event.
     * 
     * @param event The event to handle
     */
    protected void handleServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event) {
        // Do nothing by default
    }

    /**
     * Handles the given service candidate entry event.
     * 
     * @param event The event to handle
     */
    protected void handleServiceCandidateEntryEvent(ServiceCandidateEntryEvent event) {
        // Do nothing by default
    }

    /**
     * Handles the given service candidate exit event.
     * 
     * @param event The event to handle
     */
    protected void handleServiceCandidateExitEvent(ServiceCandidateExitEvent event) {
        // Do nothing by default
    }

    /**
     * Handles the given service candidate return event.
     * 
     * @param event The event to handle
     */
    protected void handleServiceCandidateReturnEvent(ServiceCandidateReturnEvent event) {
        // Do nothing by default
    }

    /**
     * Handles the given implicit transaction abort event.
     * 
     * @param event The event to handle
     */
    protected void handleImplicitTransactionAbortEvent(ImplicitTransactionAbortEvent event) {
        // Do nothing by default
    }

    /**
     * Handles the given explicit transaction abort event.
     * 
     * @param event The event to handle
     */
    protected void handleExplicitTransactionAbortEvent(ExplicitTransactionAbortEvent event) {
        // Do nothing by default
    }

    /**
     * Handles the given transaction commit event.
     * 
     * @param event The event to handle
     */
    protected void handleTransactionCommitEvent(TransactionCommitEvent event) {
        // Do nothing by default
    }

    /**
     * Handles the given transaction start event.
     * 
     * @param event The event to handle
     */
    protected void handleTransactionStartEvent(TransactionStartEvent event) {
        // Do nothing by default
    }

    /**
     * Handles the given use case end event.
     * 
     * @param event The event to handle
     */
    protected void handleUseCaseEndEvent(UseCaseEndEvent event) {
        // Do nothing by default
    }

    /**
     * Handles the given use case start event.
     * 
     * @param event The event to handle
     */
    protected void handleUseCaseStartEvent(UseCaseStartEvent event) {
        // Do nothing by default
    }

}
