package gutta.prediction.event;

/**
 * This event represents the start of a use case and is expected to be the first event in a trace.
 * 
 * @param traceId   The id of the trace containing this event
 * @param timestamp The timestamp at which this event occurred
 * @param location  The location at which this timestamp occurred
 * @param name      The name of the use case
 */
public record UseCaseStartEvent(long traceId, long timestamp, Location location, String name) implements UseCaseEvent {

    @Override
    public int hashCode() {
        return (int) (this.traceId() + this.timestamp());
    }

}
