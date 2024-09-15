package gutta.prediction.domain;

public class RemoteComponentConnection extends ComponentConnection {
    
    private final boolean symmetric;
    
    private final long latency;
    
    private final TransactionPropagation transactionPropagation;
    
    public RemoteComponentConnection(Component source, Component target, boolean symmetric, long latency, TransactionPropagation transactionPropagation, boolean synthetic) {
        super(source, target, synthetic);
        
        this.symmetric = symmetric;
        this.latency = latency;
        this.transactionPropagation = transactionPropagation;
    }
        
    public enum TransactionPropagation {
        IDENTICAL,
        SUBORDINATE,
        NONE
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
    
}
