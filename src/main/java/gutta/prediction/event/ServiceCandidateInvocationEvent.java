package gutta.prediction.event;

public record ServiceCandidateInvocationEvent(long traceId, long timestamp, Location location, String name) implements ServiceCandidateEvent {

    @Override
    public int hashCode() {
        return (int) (this.traceId() + this.timestamp());
    }
    
}
