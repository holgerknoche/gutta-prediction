package gutta.prediction.span;

import gutta.prediction.util.EqualityUtil;

public final class SuspendedTransactionOverlay extends TransactionOverlay {
    
    private final boolean dirty;
    
    public SuspendedTransactionOverlay(long startTimestamp, boolean dirty) {
        super(startTimestamp);
        
        this.dirty = dirty;
    }
    
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

}
