package gutta.prediction.event;

public record ServiceCandidateEntryEvent(long traceId, long timestamp, Location location, String name, boolean transactionStarted, String transactionId) implements MonitoringEvent {

    public ServiceCandidateEntryEvent(long traceId, long timestamp, Location location, String name) {
        this(traceId, timestamp, location, name, false, null);
    }
    
}
