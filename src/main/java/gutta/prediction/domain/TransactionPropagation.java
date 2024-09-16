package gutta.prediction.domain;

public enum TransactionPropagation {
    IDENTICAL {

        @Override
        public boolean canPropagateTransactions() {
            return true;
        }

    },
    SUBORDINATE {

        @Override
        public boolean canPropagateTransactions() {
            return true;
        }

    },
    NONE {

        @Override
        public boolean canPropagateTransactions() {
            return false;
        }
    };

    public abstract boolean canPropagateTransactions();

}
