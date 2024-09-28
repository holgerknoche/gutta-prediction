package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.Entity;
import gutta.prediction.event.EntityWriteEvent;

public record WriteConflictIssue(Entity entity, EntityWriteEvent event) implements ConsistencyIssue<EntityWriteEvent> {

}
