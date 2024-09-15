package gutta.prediction.domain;

import gutta.prediction.domain.RemoteComponentConnection.TransactionPropagation;

public class LocalComponentConnection extends ComponentConnection {
    
    public LocalComponentConnection(Component source, Component target, boolean synthetic) {
        super(source, target, synthetic);
    }
    
    @Override
    public boolean isSymmetric() {
        return true;
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

}
