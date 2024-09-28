package gutta.prediction.event;

import gutta.prediction.domain.Entity;

/**
 * This event represents a write access to a specific data entity.
 * 
 * @param traceId The id of the trace containing this event
 * @param timestamp The timestamp at which this event occurred
 * @param location The location at which this timestamp occurred
 * @param entity The written entity
 */
public record EntityWriteEvent(long traceId, long timestamp, Location location, Entity entity) implements MonitoringEvent {

    @Override
    public <R> R accept(MonitoringEventVisitor<R> visitor) {
        return visitor.handleEntityWriteEvent(this);
    }

}
