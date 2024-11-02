package gutta.prediction.event;

public record UseCaseEndEvent(long traceId, long timestamp, Location location, String name) implements UseCaseEvent {
    
    @Override
    public int hashCode() {
        return (int) (this.traceId() + this.timestamp());
    }
    
}
