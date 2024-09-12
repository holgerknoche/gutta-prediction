package gutta.prediction.event;

public record TransactionCommitEvent(long traceId, long timestamp, Location location, String transactionId) implements MonitoringEvent {

    @Override
    public <R> R accept(MonitoringEventVisitor<R> visitor) {
        return visitor.handleTransactionCommitEvent(this);
    }

}
