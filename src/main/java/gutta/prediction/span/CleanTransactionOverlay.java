package gutta.prediction.span;

import gutta.prediction.util.EqualityUtil;

/**
 * A {@link CleanTransactionOverlay} represents a span overlay representing a <i>clean</i> transaction state, i.e., with no pending writes.
 */
public final class CleanTransactionOverlay extends TransactionOverlay {

    /**
     * Creates a new overlay with the given start timestamp.
     * 
     * @param startTimestamp The start timestamp of the overlay
     */
    CleanTransactionOverlay(long startTimestamp) {
        super(startTimestamp);
    }

    /**
     * Creates a new overlay with the given start and end timestamps.
     * 
     * @param startTimestamp The start timestamp of the overlay
     * @param endTimestamp   The end timestamp of the overlay
     */
    public CleanTransactionOverlay(long startTimestamp, long endTimestamp) {
        super(startTimestamp, endTimestamp);
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public <R> R accept(TraceElementVisitor<R> visitor) {
        return visitor.handleCleanTransactionOverlay(this);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }

    private boolean equalsInternal(CleanTransactionOverlay that) {
        return super.equalsInternal(that);
    }

    @Override
    public String toString() {
        return "Clean TX overlay [" + this.startTimestamp() + " -- " + this.endTimestamp() + "]";
    }

}
