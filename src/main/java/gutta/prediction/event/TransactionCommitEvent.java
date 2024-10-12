package gutta.prediction.event;

public record TransactionCommitEvent(long traceId, long timestamp, Location location, String transactionId) implements MonitoringEvent {

}
