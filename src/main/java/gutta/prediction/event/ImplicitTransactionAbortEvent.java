package gutta.prediction.event;

/**
 * This event represents an implicit abort of a transaction, i.e., an abort that is caused by an exception being thrown.
 * 
 * @param traceId       The id of the trace containing this event
 * @param timestamp     The timestamp at which this event occurred
 * @param location      The location at which this timestamp occurred
 * @param transactionId The id of the transaction to be aborted
 * @param cause         The cause of the implicit abort
 */
public record ImplicitTransactionAbortEvent(long traceId, long timestamp, Location location, String transactionId, String cause) implements TransactionEvent {

    @Override
    public int hashCode() {
        return (int) (this.traceId() + this.timestamp());
    }

}
