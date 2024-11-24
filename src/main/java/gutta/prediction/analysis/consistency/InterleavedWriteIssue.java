package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.Entity;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.MonitoringEvent;

/**
 * Issue representing an interleaved write access, i.e., a sequence of writes to the same aggregate with a write to another component in between.
 */
public class InterleavedWriteIssue extends ConsistencyIssue<EntityWriteEvent> {

    /**
     * Creates a new issues with the given data.
     * 
     * @param entity The entity accessed
     * @param event  The event that caused the issue
     */
    public InterleavedWriteIssue(Entity entity, EntityWriteEvent event) {
        super(entity, event);
    }

    @Override
    ConsistencyIssue<EntityWriteEvent> rewriteToEvent(MonitoringEvent event) {
        return new InterleavedWriteIssue(this.entity(), (EntityWriteEvent) event);
    }

    @Override
    public <R> R accept(ConsistencyIssueVisitor<R> visitor) {
        return visitor.handleInterleavedWriteIssue(this);
    }

    @Override
    public String description() {
        return "Interleaved Write";
    }

}
