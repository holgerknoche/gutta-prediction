package gutta.prediction.span;

import gutta.prediction.util.EqualityUtil;

/**
 * Abstract supertype for all span events.
 */
public abstract sealed class SpanEvent implements TraceElement permits ConsistencyIssueEvent, EntityEvent, TransactionEvent {

    private final long timestamp;

    /**
     * Creates a new span event at the given timestamp.
     * 
     * @param timestamp The timestamp at which the event occurred
     */
    protected SpanEvent(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the timestamp at which this event occurred.
     * 
     * @return see above
     */
    public long timestamp() {
        return this.timestamp;
    }

    @Override
    public int hashCode() {
        return (int) this.timestamp();
    }

    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }

    /**
     * Compares this span event to another. This method is only intended to be invoked by {@link #equals(Object)} or subtypes.
     * 
     * @param that The span event to compare to
     * @return {@code True} if the objects are equal, {@code false} otherwise
     */
    protected boolean equalsInternal(SpanEvent that) {
        return (this.timestamp() == that.timestamp());
    }

}
