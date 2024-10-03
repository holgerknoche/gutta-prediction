package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.Entity;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.util.EqualityUtil;

import java.util.Objects;

public abstract class ConsistencyIssue<T extends MonitoringEvent> {

    private final Entity entity;
    
    private final T event;

    protected ConsistencyIssue(Entity entity, T event) {
        this.entity = entity;
        this.event = event;
    }
    
    public Entity entity() {
        return this.entity;
    }

    public T event() {
        return this.event;
    }

    abstract ConsistencyIssue<T> rewriteToEvent(MonitoringEvent event);
    
    @Override
    public int hashCode() {
        return Objects.hash(this.entity, this.event);
    }
    
    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    protected boolean equalsInternal(ConsistencyIssue<T> that) {
        return Objects.equals(this.entity, that.entity) &&
                Objects.equals(this.event,  that.event);
    }

}
