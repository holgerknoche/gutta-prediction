package gutta.prediction.domain;

/**
 * A {@link ServiceCandidate} represents a to-be service within a {@linkplain DeploymentModel deployment model}.
 * 
 * @param name                The candidate's name
 * @param transactionBehavior The candidate's transaction behavior, e.g., whether or not it will reuse an existing transaction
 * @param asynchronous        Denotes whether this candidate is invoked asynchronously
 */
public record ServiceCandidate(String name, TransactionBehavior transactionBehavior, boolean asynchronous) {

    /**
     * Creates a service candidate with default transaction behavior.
     * 
     * @param name The candidate's name
     */
    public ServiceCandidate(String name) {
        this(name, TransactionBehavior.defaultBehavior());
    }

    /**
     * Creates a service candidate with the given transaction behavior.
     * 
     * @param name                The candidate's name
     * @param transactionBehavior The candidate's transaction behavior, e.g., whether or not it will reuse an existing transaction
     */
    public ServiceCandidate(String name, TransactionBehavior transactionBehavior) {
        this(name, transactionBehavior, false);
    }

}
