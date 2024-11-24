package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.Entity;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.util.EqualityUtil;

/**
 * Issue representing a read that causes a write conflict.
 */
public class WriteConflictIssue extends ConsistencyIssue<EntityWriteEvent> {

    /**
     * Creates a new issues with the given data.
     * 
     * @param entity The entity accessed
     * @param event  The event that caused the issue
     */
    public WriteConflictIssue(Entity entity, EntityWriteEvent event) {
        super(entity, event);
    }
    
    @Override
    ConsistencyIssue<EntityWriteEvent> rewriteToEvent(MonitoringEvent event) {
        return new WriteConflictIssue(this.entity(), (EntityWriteEvent) event);
    }
    
    @Override
    public String description() {
        return "Write Conflict";
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    private boolean equalsInternal(WriteConflictIssue that) {
        return super.equalsInternal(that);
    }
    
    @Override
    public <R> R accept(ConsistencyIssueVisitor<R> visitor) {
        return visitor.handleWriteConflictIssue(this);
    }

}
