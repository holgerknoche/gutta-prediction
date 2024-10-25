package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.Entity;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.util.EqualityUtil;

public class PotentialDeadlockIssue extends ConsistencyIssue<EntityReadEvent> {
        
    public PotentialDeadlockIssue(Entity entity, EntityReadEvent event) {
        super(entity, event);
    }
        
    @Override
    ConsistencyIssue<EntityReadEvent> rewriteToEvent(MonitoringEvent event) {
        return new PotentialDeadlockIssue(this.entity(), (EntityReadEvent) event);
    }
    
    @Override
    public String description() {
        return "Potential Deadlock";
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    private boolean equalsInternal(PotentialDeadlockIssue that) {
        return super.equalsInternal(that);
    }
    
    @Override
    public <R> R accept(ConsistencyIssueVisitor<R> visitor) {
        return visitor.handlePotentialDeadlockIssue(this);
    }

}
