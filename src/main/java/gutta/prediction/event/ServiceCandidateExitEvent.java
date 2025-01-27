package gutta.prediction.event;

/**
 * This event represents the exit from an invoked service candidate (on the implementor's side) and thus marks the start of the return overhead.
 * 
 * @param traceId   The id of the trace containing this event
 * @param timestamp The timestamp at which this event occurred
 * @param location  The location at which this timestamp occurred
 * @param name      The name of the invoked service candidate
 */
public record ServiceCandidateExitEvent(long traceId, long timestamp, Location location, String name) implements ServiceCandidateEvent {

    @Override
    public int hashCode() {
        return (int) (this.traceId() + this.timestamp());
    }

}
