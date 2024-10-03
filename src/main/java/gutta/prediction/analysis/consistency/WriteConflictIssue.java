package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.Entity;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.MonitoringEvent;

public record WriteConflictIssue(Entity entity, EntityWriteEvent event) implements ConsistencyIssue<EntityWriteEvent> {

    @Override
    public ConsistencyIssue<EntityWriteEvent> rewriteToEvent(MonitoringEvent event) {
        return new WriteConflictIssue(this.entity(), (EntityWriteEvent) event);
    }

}
