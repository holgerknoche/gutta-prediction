package gutta.prediction.domain;

public record ServiceCandidate(String name, TransactionBehavior transactionBehavior) {
    
    public ServiceCandidate(String name) {
        this(name, TransactionBehavior.SUPPORTED);
    }
    
}
