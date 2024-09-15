package gutta.prediction.domain;

public record ServiceCandidate(String name, TransactionMode transactionMode) {
    
    public enum TransactionMode {
        MANDATORY,
        NEVER,
        NOT_SUPPORTED,
        REQUIRED,
        REQUIRES_NEW,
        SUPPORTED
    }
    
}
