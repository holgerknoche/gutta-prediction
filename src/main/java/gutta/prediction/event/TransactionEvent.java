package gutta.prediction.event;

/**
 * A {@link TransactionEvent} is a supertype for all events related to a transaction, such as commit events.
 */
public sealed interface TransactionEvent extends MonitoringEvent
        permits ExplicitTransactionAbortEvent, ImplicitTransactionAbortEvent, TransactionStartEvent, TransactionCommitEvent {

    /**
     * Returns the id of the transaction affected by this event.
     * 
     * @return see above
     */
    String transactionId();

}
