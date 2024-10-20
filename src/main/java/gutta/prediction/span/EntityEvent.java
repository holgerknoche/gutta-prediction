package gutta.prediction.span;

import gutta.prediction.domain.Entity;
import gutta.prediction.util.EqualityUtil;

import java.util.Objects;

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
    
    @Override
    public int hashCode() {
        return (int) this.timestamp() + this.type.hashCode();
    } 
    
    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    private boolean equalsInternal(EntityEvent that) {
        if (!super.equalsInternal(that)) {
            return false;
        }
        
        return (this.type() == that.type()) &&
                Objects.equals(this.entity(), that.entity());
    }
    
    @Override
    public String toString() {
        return "Entity Event '" + this.type() + "' at " + this.timestamp() + " for entity " + this.entity();
    }
    
    public enum EntityEventType {
        READ,
        WRITE
    }    

}
