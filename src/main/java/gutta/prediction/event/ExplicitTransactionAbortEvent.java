package gutta.prediction.event;

/**
 * This event represents an explicit abort of a transaction, e.g., by explicitly issuing a rollback command.
 * 
 * @param traceId       The id of the trace containing this event
 * @param timestamp     The timestamp at which this event occurred
 * @param location      The location at which this timestamp occurred
 * @param transactionId The id of the aborted transaction
 */
public record ExplicitTransactionAbortEvent(long traceId, long timestamp, Location location, String transactionId) implements TransactionEvent {
    
    @Override
    public int hashCode() {
        return (int) (this.traceId() + this.timestamp());
    }
    
}
