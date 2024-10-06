package gutta.prediction.span;

import gutta.prediction.util.EqualityUtil;

public final class CleanTransactionOverlay extends TransactionOverlay {
    
    CleanTransactionOverlay(long startTimestamp) {
        super(startTimestamp);
    }
    
    CleanTransactionOverlay(long startTimestamp, long endTimestamp) {
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
    
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    private boolean equalsInternal(CleanTransactionOverlay that) {
        return super.equalsInternal(that);
    }

}
