package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.Entity;
import gutta.prediction.event.EntityReadEvent;

public record StaleReadIssue(Entity entity, EntityReadEvent event) implements ConsistencyIssue<EntityReadEvent> {

}
