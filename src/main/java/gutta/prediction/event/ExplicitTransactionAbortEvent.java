package gutta.prediction.event;

public record ExplicitTransactionAbortEvent(long traceId, long timestamp, Location location, String transactionId) implements MonitoringEvent {
    
}
