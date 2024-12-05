package gutta.prediction.span;

/**
 * Abstract superclass for all span overlays related to transaction state.
 */
public abstract sealed class TransactionOverlay extends SpanOverlay permits CleanTransactionOverlay, DirtyTransactionOverlay, SuspendedTransactionOverlay {

    /**
     * Creates a new transaction overlay with the given start timestamp.
     * 
     * @param startTimestamp The start timestamp of the overlay
     */
    protected TransactionOverlay(long startTimestamp) {
        super(startTimestamp);
    }

    /**
     * Creates a new transaction overlay with the given start and end timestamps.
     * 
     * @param startTimestamp The start timestamp of the overlay
     * @param endTimestamp   The end timestamp of the overlay
     */
    protected TransactionOverlay(long startTimestamp, long endTimestamp) {
        super(startTimestamp, endTimestamp);
    }

    /**
     * Denotes whether this overlay represents a <i>dirty</i> state, i.e., whether the transaction has pending writes.
     * 
     * @return see above
     */
    public abstract boolean isDirty();

    /**
     * Denotes whether this overlay represents a suspended transaction state.
     * 
     * @return see above
     */
    public boolean isSuspended() {
        return false;
    }

}
