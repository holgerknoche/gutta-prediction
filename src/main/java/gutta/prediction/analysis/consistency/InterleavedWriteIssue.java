package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.Entity;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.MonitoringEvent;

public class InterleavedWriteIssue extends ConsistencyIssue<EntityWriteEvent> {

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
