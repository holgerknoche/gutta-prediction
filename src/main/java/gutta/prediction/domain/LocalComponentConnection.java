package gutta.prediction.domain;

import gutta.prediction.util.EqualityUtil;

public final class LocalComponentConnection extends ComponentConnection {
    
    public LocalComponentConnection(Component source, Component target, boolean modified) {
        super(source, target, modified);
    }
    
    @Override
    public long latency() {
        return 0;
    }

    @Override
    public boolean isRemote() {
        return false;
    }
    
    @Override
    public TransactionPropagation transactionPropagation() {
        return TransactionPropagation.IDENTICAL;
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    private boolean equalsInternal(LocalComponentConnection that) {
        return super.equalsInternal(that);
    }
    
    @Override
    public String toString() {
        return "local " + this.source() + " -- " + this.target();
    }

}
