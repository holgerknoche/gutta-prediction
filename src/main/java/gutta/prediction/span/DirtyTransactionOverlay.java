package gutta.prediction.span;

import gutta.prediction.util.EqualityUtil;

/**
 * A {@link DirtyTransactionOverlay} represents a span overlay representing a <i>dirty</i> transaction state, i.e., with pending writes.
 */
public final class DirtyTransactionOverlay extends TransactionOverlay {
    
    /**
     * Creates a new overlay with the given start timestamp.
     * 
     * @param startTimestamp The start timestamp of the overlay
     */
    DirtyTransactionOverlay(long startTimestamp) {
        super(startTimestamp);
    }
    
    /**
     * Creates a new overlay with the given start and end timestamps.
     * 
     * @param startTimestamp The start timestamp of the overlay
     * @param endTimestamp   The end timestamp of the overlay
     */
    public DirtyTransactionOverlay(long startTimestamp, long endTimestamp) {
        super(startTimestamp, endTimestamp);
    }

    @Override
    public boolean isDirty() {
        return true;
    }
    
    @Override
    public <R> R accept(TraceElementVisitor<R> visitor) {
        return visitor.handleDirtyTransactionOverlay(this);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    private boolean equalsInternal(DirtyTransactionOverlay that) {
        return super.equalsInternal(that);
    }
    
    @Override
    public String toString() {
        return "Dirty TX overlay [" + this.startTimestamp() + " -- " + this.endTimestamp() + "]";
    }
    
}
