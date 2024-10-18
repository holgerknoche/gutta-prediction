package gutta.prediction.event;

import gutta.prediction.domain.Entity;

/**
 * This event represents a read access to a specific data entity.
 * 
 * @param traceId The id of the trace containing this event
 * @param timestamp The timestamp at which this event occurred
 * @param location The location at which this timestamp occurred
 * @param entity The read entity
 */
public record EntityReadEvent(long traceId, long timestamp, Location location, Entity entity) implements EntityAccessEvent {

}
