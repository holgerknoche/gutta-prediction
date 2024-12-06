package gutta.prediction.span;

import gutta.prediction.util.EqualityUtil;

/**
 * Abstract supertype for all span overlays.
 */
public abstract class SpanOverlay extends Interval implements TraceElement {

    /**
     * Creates a new overlay with the given start timestamp.
     * 
     * @param startTimestamp The start timestamp of the overlay
     */
    protected SpanOverlay(long startTimestamp) {
        this(startTimestamp, 0);
    }

    /**
     * Creates a new overlay with the given start and end timestamps.
     * 
     * @param startTimestamp The start timestamp of the overlay
     * @param endTimestamp   The end timestamp of the overlay
     */
    protected SpanOverlay(long startTimestamp, long endTimestamp) {
        super(startTimestamp, endTimestamp);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Compares this span overlay to another. This method is only intended to be invoked by {@link #equals(Object)} or subtypes.
     * 
     * @param that The span overlay to compare to
     * @return {@code True} if the objects are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }

    /**
     * Compares this span overlay to another. This method is only intended to be invoked by {@link #equals(Object)} or subtypes.
     * 
     * @param that The span overlay to compare to
     * @return {@code True} if the objects are equal, {@code false} otherwise
     */
    protected boolean equalsInternal(SpanOverlay that) {
        return super.equalsInternal(that);
    }

    @Override
    public void traverse(TraceElementVisitor<?> visitor) {
        this.accept(visitor);
    }

}
