package gutta.prediction.domain;

/**
 * Enumeration of all possible transaction behaviors of a {@linkplain ServiceCandidate service candidate}, based on the modes available in Jakarta EE.
 */
public enum TransactionBehavior {

    /**
     * Denotes that an existing transaction is necessary to invoke the service candidate, and the invocation will fail if none is available.
     */
    MANDATORY,
    
    /**
     * Denotes that no transaction must exist on invoking the service candidate, and the invocation will fail if one is available.
     */
    NEVER,
    
    /**
     * Denotes that the service candidate will suspend an existing transaction, and do nothing if none is available.
     */
    NOT_SUPPORTED,
    
    /**
     * Denotes that the service candidate will create a new transaction if none is available, and do nothing if one is available.
     */
    REQUIRED,
    
    /**
     * Denotes that the service candidate will always create a new transaction, and suspend an existing one if available.
     */
    REQUIRES_NEW,
    
    /**
     * Denotes that the service candidate will reuse a transaction if one is available, and do nothing if none is available.
     */
    SUPPORTED;

    /**
     * Returns the default transaction behavior that is used if none is specified.
     * 
     * @return see above
     */
    public static TransactionBehavior defaultBehavior() {
        return SUPPORTED;
    }

}
