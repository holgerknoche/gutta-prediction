package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.Entity;
import gutta.prediction.event.EntityAccessEvent;
import gutta.prediction.event.MonitoringEvent;

public class CrossComponentAccessIssue extends ConsistencyIssue<EntityAccessEvent> {

    public CrossComponentAccessIssue(Entity entity, EntityAccessEvent event) {
        super(entity, event);
    }

    @Override
    ConsistencyIssue<EntityAccessEvent> rewriteToEvent(MonitoringEvent event) {
        return new CrossComponentAccessIssue(this.entity(), (EntityAccessEvent) event);
    }

    @Override
    public <R> R accept(ConsistencyIssueVisitor<R> visitor) {
        return visitor.handleCrossComponentAccessIssue(this);
    }

    @Override
    public String description() {
        return "Cross-Component Access";
    }

}
