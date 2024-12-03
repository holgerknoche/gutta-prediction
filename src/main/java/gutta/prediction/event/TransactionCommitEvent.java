package gutta.prediction.event;

/**
 * This event represents an explicit commit of a transaction, e.g., by explicitly issuing an appropriate command.
 * 
 * @param traceId       The id of the trace containing this event
 * @param timestamp     The timestamp at which this event occurred
 * @param location      The location at which this timestamp occurred
 * @param transactionId The id of the committed transaction
 */
public record TransactionCommitEvent(long traceId, long timestamp, Location location, String transactionId) implements TransactionEvent {

    @Override
    public int hashCode() {
        return (int) (this.traceId() + this.timestamp());
    }

}
