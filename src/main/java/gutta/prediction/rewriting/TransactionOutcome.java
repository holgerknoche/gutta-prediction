package gutta.prediction.rewriting;

enum TransactionOutcome {
    COMPLETED {
        
        @Override
        public TransactionOutcome joinWith(TransactionOutcome other) {
            return other;
        }
        
    },
    ABORTED {
        
        @Override
        public TransactionOutcome joinWith(TransactionOutcome other) {
            return this;
        }
        
    },
    KEPT {
        
        @Override
        public TransactionOutcome joinWith(TransactionOutcome other) {
            return (other == ABORTED) ? other : this;
        }
        
    };
    
    public abstract TransactionOutcome joinWith(TransactionOutcome other);
    
}
