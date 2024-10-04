package gutta.prediction.span;

import gutta.prediction.analysis.consistency.ConsistencyIssue;

public class ConsistencyIssueEvent extends SpanEvent {
    
    private final ConsistencyIssue<?> issue;
    
    public ConsistencyIssueEvent(long timestamp, ConsistencyIssue<?> issue) {
        super(timestamp);
        
        this.issue = issue;
    }
    
    public ConsistencyIssue<?> issue() {
        return this.issue;
    }

}
