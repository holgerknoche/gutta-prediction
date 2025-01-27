package gutta.prediction.span;

import gutta.prediction.analysis.consistency.ConsistencyIssue;

/**
 * A {@link ConsistencyIssueEvent} is a span event that represents the occurrence {@linkplain ConsistencyIssue consistency issue} in a span.
 */
public final class ConsistencyIssueEvent extends SpanEvent {

    private final ConsistencyIssue<?> issue;

    /**
     * Creates a new event for the given issue.
     * 
     * @param issue The consistency issue to be associated with the event
     */
    public ConsistencyIssueEvent(ConsistencyIssue<?> issue) {
        super(issue.event().timestamp());

        this.issue = issue;
    }

    /**
     * Returns the consistency issue associated with this event.
     * 
     * @return see above
     */
    public ConsistencyIssue<?> issue() {
        return this.issue;
    }

    @Override
    public <R> R accept(TraceElementVisitor<R> visitor) {
        return visitor.handleConsistencyIssueEvent(this);
    }

    @Override
    public void traverse(TraceElementVisitor<?> visitor) {
        this.accept(visitor);
    }

}
