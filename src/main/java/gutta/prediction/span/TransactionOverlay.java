package gutta.prediction.span;

abstract sealed class TransactionOverlay extends SpanOverlay permits CleanTransactionOverlay, DirtyTransactionOverlay, SuspendedTransactionOverlay {
    
    protected TransactionOverlay(long startTimestamp) {
        super(startTimestamp);
    }
    
    protected TransactionOverlay(long startTimestamp, long endTimestamp) {
        super(startTimestamp, endTimestamp);
    }
    
    public abstract boolean isDirty();

}
