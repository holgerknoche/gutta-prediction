package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.Entity;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.util.EqualityUtil;

public class WriteConflictIssue extends ConsistencyIssue<EntityWriteEvent> {

    WriteConflictIssue(Entity entity, EntityWriteEvent event) {
        super(entity, event);
    }
    
    @Override
    ConsistencyIssue<EntityWriteEvent> rewriteToEvent(MonitoringEvent event) {
        return new WriteConflictIssue(this.entity(), (EntityWriteEvent) event);
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
