package gutta.prediction.domain;

import gutta.prediction.util.EqualityUtil;

import java.util.Objects;

public abstract class ComponentConnection {

    private final Component source;
    
    private final Component target;
    
    private final boolean modified;
        
    protected ComponentConnection(Component source, Component target, boolean modified) {
        this.source = source;
        this.target = target;
        this.modified = modified;        
    }
    
    public Component source() {
        return this.source;
    }
    
    public Component target() {
        return this.target;
    }        
    
    public boolean isModified() {
        return this.modified;
    }
    
    public abstract long overhead();
        
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
                (this.modified == that.modified);
    }

}
