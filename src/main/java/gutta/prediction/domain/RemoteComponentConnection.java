package gutta.prediction.domain;

import gutta.prediction.util.EqualityUtil;

public final class RemoteComponentConnection extends ComponentConnection {
    
    private final boolean symmetric;
    
    private final long latency;
    
    private final TransactionPropagation transactionPropagation;
    
    public RemoteComponentConnection(Component source, Component target, boolean symmetric, long latency, TransactionPropagation transactionPropagation, boolean synthetic) {
        super(source, target, synthetic);
        
        this.symmetric = symmetric;
        this.latency = latency;
        this.transactionPropagation = transactionPropagation;
    }

    @Override
    public long latency() {
        return this.latency;
    }

    @Override
    public boolean isSymmetric() {
        return this.symmetric;
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
        return super.hashCode() + (int) this.latency;
    }
    
    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    private boolean equalsInternal(RemoteComponentConnection that) {
        if (!super.equalsInternal(that)) {
            return false;
        }
        
        return (this.symmetric == that.symmetric) &&
                (this.latency == that.latency) &&
                (this.transactionPropagation == that.transactionPropagation);
    }
    
}
