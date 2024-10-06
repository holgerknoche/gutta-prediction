package gutta.prediction.span;

import gutta.prediction.util.EqualityUtil;

public final class DirtyTransactionOverlay extends TransactionOverlay {
    
    DirtyTransactionOverlay(long startTimestamp) {
        super(startTimestamp);
    }
    
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
    
}
