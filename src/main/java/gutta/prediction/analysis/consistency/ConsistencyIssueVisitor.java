package gutta.prediction.analysis.consistency;

public interface ConsistencyIssueVisitor<R> {
    
    default R handlePotentialDeadlockIssue(PotentialDeadlockIssue issue) {
        // Do nothing by default
        return null;
    }
    
    default R handleStaleReadIssue(StaleReadIssue issue) {
        // Do nothing by default
        return null;
    }
    
    default R handleWriteConflictIssue(WriteConflictIssue issue) {
        // Do nothing by default
        return null;
    }
    
    default R handleCrossComponentAccessIssue(CrossComponentAccessIssue issue) {
        // Do nothing by default
        return null;
    }

}
