package gutta.prediction.event;

import gutta.prediction.domain.Entity;

/**
 * An {@EntityAccessEvent} is a supertype for all events that represent an access to an {@link Entity}, such as read or write events.
 */
public sealed interface EntityAccessEvent extends MonitoringEvent permits EntityReadEvent, EntityWriteEvent {

    /**
     * Returns the entity affected by this event.
     * 
     * @return see above
     */
    Entity entity();

}
