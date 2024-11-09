package gutta.prediction.domain;

import gutta.prediction.util.EqualityUtil;

public final class RemoteComponentConnection extends ComponentConnection {
        
    private final long overhead;
    
    private final TransactionPropagation transactionPropagation;
    
    public RemoteComponentConnection(Component source, Component target, long overhead, TransactionPropagation transactionPropagation, boolean modified) {
        super(source, target, modified);
        
        this.overhead = overhead;
        this.transactionPropagation = transactionPropagation;
    }

    @Override
    public long overhead() {
        return this.overhead;
    }

    @Override
    public boolean isRemote() {
        return true;
    }

    @Override
    public TransactionPropagation transactionPropagation() {
        return this.transactionPropagation;
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() + (int) this.overhead;
    }
    
    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    private boolean equalsInternal(RemoteComponentConnection that) {
        if (!super.equalsInternal(that)) {
            return false;
        }
        
        return (this.overhead == that.overhead) &&
                (this.transactionPropagation == that.transactionPropagation);
    }
    
    @Override
    public String toString() {
        return "remote " + this.source() + " -> " + this.target();
    }
    
}
