package gutta.prediction.span;

public interface TraceElementVisitor<T> {
    
    default T handleTrace(Trace trace) {
        // Do nothing by default
        return null;
    }
    
    default T handleSpan(Span span) {
        // Do nothing by default
        return null;
    }
    
    default T handleCleanTransactionOverlay(CleanTransactionOverlay overlay) {
        // Do nothing by default
        return null;
    }
    
    default T handleDirtyTransactionOverlay(DirtyTransactionOverlay overlay) {
        // Do nothing by default
        return null;
    }
    
    default T handleSuspendedTransactionOverlay(SuspendedTransactionOverlay overlay) {
        // Do nothing by default
        return null;
    }
    
    default T handleLatencyOverlay(LatencyOverlay overlay) {
        // Do nothing by default
        return null;
    }
    
    default T handleConsistencyIssueEvent(ConsistencyIssueEvent event) {
        // Do nothing by default
        return null;
    }
    
    default T handleTransactionEvent(TransactionEvent event) {
        // Do nothing by default
        return null;
    }
    
    default T handleEntityEvent(EntityEvent event) {
        // Do nothing by default
        return null;
    }

}
