package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.Entity;
import gutta.prediction.event.EntityAccessEvent;
import gutta.prediction.event.MonitoringEvent;

/**
 * Issue representing a cross-component access to an entity.
 */
public class CrossComponentAccessIssue extends ConsistencyIssue<EntityAccessEvent> {

    /**
     * Creates a new issues with the given data.
     * 
     * @param entity The entity accessed
     * @param event  The event that caused the issue
     */
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
