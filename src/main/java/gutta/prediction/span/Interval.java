package gutta.prediction.span;

import gutta.prediction.util.EqualityUtil;

/**
 * An {@link Interval} is an abstract type for span elements representing a time interval.
 */
public abstract class Interval {

    private final long startTimestamp;

    private long endTimestamp;

    /**
     * Creates a new interval with the given start and end timestamps.
     * 
     * @param startTimestamp The start timestamp of the interval
     * @param endTimestamp   The end timestamp of the interval
     */
    protected Interval(long startTimestamp, long endTimestamp) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }

    /**
     * Returns the start timestamp of the interval.
     * 
     * @return see above
     */
    public long startTimestamp() {
        return this.startTimestamp;
    }

    /**
     * Returns the end timestamp of the interval.
     * 
     * @return see above
     */
    public long endTimestamp() {
        return this.endTimestamp;
    }

    /**
     * Sets the end timestamp of the interval.
     * 
     * @param value The end timestamp to set
     */
    void endTimestamp(long value) {
        this.endTimestamp = value;
    }

    @Override
    public int hashCode() {
        return (int) (this.startTimestamp + this.endTimestamp);
    }

    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }

    /**
     * Compares this interval to another. This method is only intended to be invoked by {@link #equals(Object)} or subtypes.
     * 
     * @param that The interval to compare to
     * @return {@code True} if the objects are equal, {@code false} otherwise
     */
    protected boolean equalsInternal(Interval that) {
        return (this.startTimestamp == that.startTimestamp) && (this.endTimestamp == that.endTimestamp);
    }

}
