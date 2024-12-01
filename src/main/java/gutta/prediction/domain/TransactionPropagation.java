package gutta.prediction.domain;

/**
 * Enumeration of all possible transaction propagation capabilities of a {@linkplain ComponentConnection component connection}.
 */
public enum TransactionPropagation {
    
    /**
     * Denotes that the connection is capable of propagating the same connection to the target component.
     * This also represents XA's concept of tightly coupled threads.
     */
    IDENTICAL {

        @Override
        public boolean canPropagateTransactions() {
            return true;
        }

    },
    
    /**
     * Denotes that the connection leads to the creation of a subordinate transaction in the target component.
     * This represents XA's concept of loosely coupled threads and the common behavior in most XA implementations.
     */
    SUBORDINATE {

        @Override
        public boolean canPropagateTransactions() {
            return true;
        }

    },

    /**
     * Denotes that the connection is unable to propagate transactions.
     */
    NONE {

        @Override
        public boolean canPropagateTransactions() {
            return false;
        }
    };

    /**
     * Denotes whether this propagation mode is capable of propagating transactions in any way.
     * 
     * @return see above
     */
    public abstract boolean canPropagateTransactions();

}
