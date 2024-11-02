package gutta.prediction.event;

public record ServiceCandidateEntryEvent(long traceId, long timestamp, Location location, String name, boolean transactionStarted, String transactionId) implements ServiceCandidateEvent {

    public ServiceCandidateEntryEvent(long traceId, long timestamp, Location location, String name) {
        this(traceId, timestamp, location, name, false, null);
    }
    
    @Override
    public int hashCode() {
        return (int) (this.traceId() + this.timestamp());
    }
    
}
