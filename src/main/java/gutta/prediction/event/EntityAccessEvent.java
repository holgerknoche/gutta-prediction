package gutta.prediction.event;

import gutta.prediction.domain.Entity;

public sealed interface EntityAccessEvent extends MonitoringEvent permits EntityReadEvent, EntityWriteEvent {
    
    Entity entity();

}
