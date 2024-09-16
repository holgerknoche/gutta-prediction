package gutta.prediction.domain;

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
    
    public abstract boolean isSymmetric();
    
    public abstract boolean isRemote();
    
    public boolean canPropagateTransactions() {
        return this.transactionPropagation().canPropagateTransactions();
    }
    
    public abstract TransactionPropagation transactionPropagation();

}
