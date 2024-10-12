package gutta.prediction.event;

public record TransactionStartEvent(long traceId, long timestamp, Location location, String transactionId) implements MonitoringEvent {
    
}
