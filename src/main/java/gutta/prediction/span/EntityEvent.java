package gutta.prediction.span;

import gutta.prediction.domain.Entity;

import static java.util.Objects.requireNonNull;

public class EntityEvent extends SpanEvent {

    private final Type type;
    
    private final Entity entity;
    
    public EntityEvent(long timestamp, Type type, Entity entity) {
        super(timestamp);
        
        this.type = requireNonNull(type);
        this.entity = requireNonNull(entity);
    }
    
    public Type type() {
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
    
    public enum Type {
        READ,
        WRITE
    }    

}
