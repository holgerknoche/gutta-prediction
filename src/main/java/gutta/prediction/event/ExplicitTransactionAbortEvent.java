package gutta.prediction.event;

public record ExplicitTransactionAbortEvent(long traceId, long timestamp, Location location, String transactionId) implements TransactionEvent {
    
    @Override
    public int hashCode() {
        return (int) (this.traceId() + this.timestamp());
    }
    
}
