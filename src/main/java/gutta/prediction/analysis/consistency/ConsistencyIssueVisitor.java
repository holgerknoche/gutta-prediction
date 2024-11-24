package gutta.prediction.analysis.consistency;

/**
 * Visitor interface for operations on {@linkplain ConsistencyIssue consistency issues}.
 * 
 * @param <R> The result type of the visitor operation
 */
public interface ConsistencyIssueVisitor<R> {

    /**
     * Handles a potential deadlock issue.
     * 
     * @param issue The issue to handle
     * @return The return of the operation
     */
    default R handlePotentialDeadlockIssue(PotentialDeadlockIssue issue) {
        // Do nothing by default
        return null;
    }

    /**
     * Handles a stale read issue.
     * 
     * @param issue The issue to handle
     * @return The return of the operation
     */
    default R handleStaleReadIssue(StaleReadIssue issue) {
        // Do nothing by default
        return null;
    }

    /**
     * Handles a write conflict issue.
     * 
     * @param issue The issue to handle
     * @return The return of the operation
     */
    default R handleWriteConflictIssue(WriteConflictIssue issue) {
        // Do nothing by default
        return null;
    }

    /**
     * Handles a cross-component access issue.
     * 
     * @param issue The issue to handle
     * @return The return of the operation
     */
    default R handleCrossComponentAccessIssue(CrossComponentAccessIssue issue) {
        // Do nothing by default
        return null;
    }

    /**
     * Handles a interleaved write issue.
     * 
     * @param issue The issue to handle
     * @return The return of the operation
     */
    default R handleInterleavedWriteIssue(InterleavedWriteIssue issue) {
        // Do nothing by default
        return null;
    }

}
