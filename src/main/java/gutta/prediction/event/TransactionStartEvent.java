package gutta.prediction.event;

public record TransactionStartEvent(long traceId, long timestamp, Location location, String transactionId, Demarcation demarcation) implements MonitoringEvent {

    @Override
    public <R> R accept(MonitoringEventVisitor<R> visitor) {
        return visitor.handleTransactionStartEvent(this);
    }
    
    public enum Demarcation {
        IMPLICIT,
        EXPLICIT
    }

}
