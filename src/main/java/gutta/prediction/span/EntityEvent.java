package gutta.prediction.span;

import gutta.prediction.domain.Entity;
import gutta.prediction.util.EqualityUtil;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * A {@link ConsistencyIssueEvent} is a span event that relates to an {@link Entity}.
 */
public class EntityEvent extends SpanEvent {

    private final EntityAccessType accessType;

    private final Entity entity;

    /**
     * Creates a new event from the given data.
     * 
     * @param timestamp  The timestamp at which this event occurred
     * @param accessType The type of entity access represented by this event
     * @param entity     The entity to be associated with this event
     */
    public EntityEvent(long timestamp, EntityAccessType accessType, Entity entity) {
        super(timestamp);

        this.accessType = requireNonNull(accessType);
        this.entity = requireNonNull(entity);
    }

    /**
     * Returns the access type represented by this event.
     * 
     * @return see above
     */
    public EntityAccessType accessType() {
        return this.accessType;
    }

    /**
     * Returns the entity associated with this event.
     * 
     * @return see above
     */
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
        return (int) this.timestamp() + this.accessType.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }

    private boolean equalsInternal(EntityEvent that) {
        if (!super.equalsInternal(that)) {
            return false;
        }

        return (this.accessType() == that.accessType()) && Objects.equals(this.entity(), that.entity());
    }

    @Override
    public String toString() {
        return "Entity Event '" + this.accessType() + "' at " + this.timestamp() + " for entity " + this.entity();
    }

    /**
     * Enumeration of all possible entity access types.
     */
    public enum EntityAccessType {
        
        /**
         * Represents a read access to an entity.
         */
        READ,
        
        /**
         * Represents a write access to an entity.
         */
        WRITE
    }

}
