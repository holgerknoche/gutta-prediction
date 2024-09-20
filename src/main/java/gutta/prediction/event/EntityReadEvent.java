package gutta.prediction.event;

/**
 * This event represents a read access to a specific data entity.
 * 
 * @param traceId The id of the trace containing this event
 * @param timestamp The timestamp at which this event occurred
 * @param location The location at which this timestamp occurred
 * @param entityType The read entity type's name
 * @param entityIdentifier The id of the read entity
 */
public record EntityReadEvent(long traceId, long timestamp, Location location, String entityType, String entityIdentifier) implements MonitoringEvent {

    @Override
    public <R> R accept(MonitoringEventVisitor<R> visitor) {
        return visitor.handleEntityReadEvent(this);
    }

}
