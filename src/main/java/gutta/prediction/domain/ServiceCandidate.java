package gutta.prediction.domain;

public record ServiceCandidate(String name, TransactionBehavior transactionBehavior, boolean asynchronous) {
    
    public ServiceCandidate(String name) {
        this(name, TransactionBehavior.SUPPORTED);
    }
    
    public ServiceCandidate(String name, TransactionBehavior transactionBehavior) {
        this(name, transactionBehavior, false);
    }
    
}
