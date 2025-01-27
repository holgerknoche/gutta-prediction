package gutta.prediction.span;

import gutta.prediction.util.EqualityUtil;

/**
 * A {@link DirtyTransactionOverlay} represents a span overlay representing a suspended transaction.
 */
public final class SuspendedTransactionOverlay extends TransactionOverlay {

    private final boolean dirty;

    /**
     * Creates a new overlay with the given start timestamp.
     * 
     * @param startTimestamp The start timestamp of the overlay
     * @param dirty          Flag denoting whether the current transaction state is dirty, i.e., has pending writes
     */
    public SuspendedTransactionOverlay(long startTimestamp, boolean dirty) {
        super(startTimestamp);

        this.dirty = dirty;
    }

    /**
     * Creates a new overlay with the given start and end timestamps.
     * 
     * @param startTimestamp The start timestamp of the overlay
     * @param endTimestamp   The end timestamp of the overlay
     * @param dirty          Flag denoting whether the current transaction state is dirty, i.e., has pending writes
     */
    public SuspendedTransactionOverlay(long startTimestamp, long endTimestamp, boolean dirty) {
        super(startTimestamp, endTimestamp);

        this.dirty = dirty;
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public boolean isSuspended() {
        return true;
    }

    @Override
    public <R> R accept(TraceElementVisitor<R> visitor) {
        return visitor.handleSuspendedTransactionOverlay(this);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }

    private boolean equalsInternal(SuspendedTransactionOverlay that) {
        if (!super.equalsInternal(that)) {
            return false;
        }

        return (this.dirty == that.dirty);
    }

    @Override
    public String toString() {
        return "Suspended TX overlay [" + this.startTimestamp() + " -- " + this.endTimestamp() + "], dirty: " + this.isDirty();
    }

}
