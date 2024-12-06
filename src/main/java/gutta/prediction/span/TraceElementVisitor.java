package gutta.prediction.span;

/**
 * Visitor interface for subtypes of {@link TraceElement}.
 * 
 * @param <T> The return type of the visitor operation
 */
public interface TraceElementVisitor<T> {

    /**
     * Handles the given trace.
     * 
     * @param trace The trace to handle
     * @return The result of the visitor operation
     */
    default T handleTrace(Trace trace) {
        // Do nothing by default
        return null;
    }

    /**
     * Handles the given span.
     * 
     * @param span The span to handle
     * @return The result of the visitor operation
     */
    default T handleSpan(Span span) {
        // Do nothing by default
        return null;
    }

    /**
     * Handles the given clean transaction overlay.
     * 
     * @param overlay The overlay to handle
     * @return The result of the visitor operation
     */
    default T handleCleanTransactionOverlay(CleanTransactionOverlay overlay) {
        // Do nothing by default
        return null;
    }

    /**
     * Handles the given dirty transaction overlay.
     * 
     * @param overlay The overlay to handle
     * @return The result of the visitor operation
     */
    default T handleDirtyTransactionOverlay(DirtyTransactionOverlay overlay) {
        // Do nothing by default
        return null;
    }

    /**
     * Handles the given suspended transaction overlay.
     * 
     * @param overlay The overlay to handle
     * @return The result of the visitor operation
     */
    default T handleSuspendedTransactionOverlay(SuspendedTransactionOverlay overlay) {
        // Do nothing by default
        return null;
    }

    /**
     * Handles the given overhead overlay.
     * 
     * @param overlay The overlay to handle
     * @return The result of the visitor operation
     */
    default T handleOverheadOverlay(OverheadOverlay overlay) {
        // Do nothing by default
        return null;
    }

    /**
     * Handles the given consistency issue event.
     * 
     * @param event The event to handle
     * @return The result of the visitor operation
     */
    default T handleConsistencyIssueEvent(ConsistencyIssueEvent event) {
        // Do nothing by default
        return null;
    }

    /**
     * Handles the given transaction event.
     * 
     * @param event The event to handle
     * @return The result of the visitor operation
     */
    default T handleTransactionEvent(TransactionEvent event) {
        // Do nothing by default
        return null;
    }

    /**
     * Handles the given entity event.
     * 
     * @param event The event to handle
     * @return The result of the visitor operation
     */
    default T handleEntityEvent(EntityEvent event) {
        // Do nothing by default
        return null;
    }

}
