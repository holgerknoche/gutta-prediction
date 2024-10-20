package gutta.prediction.span;

import gutta.prediction.domain.Entity;

import static java.util.Objects.requireNonNull;

public class EntityEvent extends SpanEvent {

    private final EntityEventType type;
    
    private final Entity entity;
    
    public EntityEvent(long timestamp, EntityEventType type, Entity entity) {
        super(timestamp);
        
        this.type = requireNonNull(type);
        this.entity = requireNonNull(entity);
    }
    
    public EntityEventType type() {
        return this.type;
    }
    
    public Entity entity() {
        return this.entity;
    }

    @Override
    public <R> R accept(TraceElementVisitor<R> visitor) {
        return visitor.handleEntityEvent(this);
    }

    @Override
    public void traverse(TraceElementVisitor<?> visitor) {
        this.accept(visitor);
    }
    
    public enum EntityEventType {
        READ,
        WRITE
    }    

}
