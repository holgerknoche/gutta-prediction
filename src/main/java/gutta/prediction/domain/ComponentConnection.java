package gutta.prediction.domain;

import gutta.prediction.util.EqualityUtil;

import java.util.Objects;

public abstract class ComponentConnection {

    private final Component source;
    
    private final Component target;
    
    private final boolean synthetic;
        
    protected ComponentConnection(Component source, Component target, boolean synthetic) {
        this.source = source;
        this.target = target;
        this.synthetic = synthetic;        
    }
    
    public Component source() {
        return this.source;
    }
    
    public Component target() {
        return this.target;
    }        
    
    public boolean isSynthetic() {
        return this.synthetic;
    }
    
    public abstract long latency();
        
    public abstract boolean isRemote();
    
    public boolean canPropagateTransactions() {
        return this.transactionPropagation().canPropagateTransactions();
    }
    
    public abstract TransactionPropagation transactionPropagation();
    
    @Override
    public int hashCode() {
        return Objects.hash(this.source, this.target);
    }
    
    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    protected boolean equalsInternal(ComponentConnection that) {
        return Objects.equals(this.source, that.source) &&
                Objects.equals(this.target, that.target) &&
                (this.synthetic == that.synthetic);
    }

}
