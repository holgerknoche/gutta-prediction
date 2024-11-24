package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.Entity;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.util.EqualityUtil;

import java.util.Objects;

/**
 * Abstract superclass for all consistency issues reported by the {@link ConsistencyIssuesAnalysis}.
 * 
 * @param <T> The type of the event that causes this issue
 */
public abstract class ConsistencyIssue<T extends MonitoringEvent> {

    private final Entity entity;

    private final T event;

    /**
     * Creates a new issue using the given data.
     * 
     * @param entity The entity affected by this issue
     * @param event  The event at which the issue occurred
     */
    protected ConsistencyIssue(Entity entity, T event) {
        this.entity = entity;
        this.event = event;
    }

    /**
     * Returns the entity affected by this issue.
     * 
     * @return see above
     */
    public Entity entity() {
        return this.entity;
    }

    /**
     * Returns the event at which this issue occurred.
     * 
     * @return see above
     */
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

    /**
     * Compares this issue to the given one. This method is intended to be called only by {@link #equals(Object)} and specializations.
     * 
     * @param that The issue to compare to
     * @return {@code True} if the two
     */
    protected boolean equalsInternal(ConsistencyIssue<T> that) {
        return Objects.equals(this.entity, that.entity) && Objects.equals(this.event, that.event);
    }

    /**
     * Accepts the given visitor and returns the result.
     * 
     * @param <R>     The result type of the visitor operation
     * @param visitor The visitor to accept
     * @return The result of the visitor operation
     */
    public abstract <R> R accept(ConsistencyIssueVisitor<R> visitor);

    /**
     * Returns a short description of this issue.
     * 
     * @return see above
     */
    public abstract String description();

}
