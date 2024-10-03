package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.Entity;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.MonitoringEvent;

public record PotentialDeadlockIssue(Entity entity, EntityReadEvent event) implements ConsistencyIssue<EntityReadEvent> {
    
    @Override
    public ConsistencyIssue<EntityReadEvent> rewriteToEvent(MonitoringEvent event) {
        return new PotentialDeadlockIssue(this.entity(), (EntityReadEvent) event);
    }

}
