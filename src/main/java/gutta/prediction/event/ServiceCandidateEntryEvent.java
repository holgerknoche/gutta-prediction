package gutta.prediction.event;

/**
 * This event represents the entry into an invoked service candidate (on the implementor's side) and thus marks the end of the invocation overhead.
 * 
 * @param traceId            The id of the trace containing this event
 * @param timestamp          The timestamp at which this event occurred
 * @param location           The location at which this timestamp occurred
 * @param name               The name of the invoked service candidate
 * @param transactionStarted Denotes whether a transaction was started upon entering the candidate
 * @param transactionId      The transaction ID of the started transaction (only if {@link #transactionStarted} is set)
 */
public record ServiceCandidateEntryEvent(long traceId, long timestamp, Location location, String name, boolean transactionStarted, String transactionId)
        implements ServiceCandidateEvent {

    /**
     * Creates a new event for an invocation that did not start a transaction.
     * 
     * @param traceId   The id of the trace containing this event
     * @param timestamp The timestamp at which this event occurred
     * @param location  The location at which this timestamp occurred
     * @param name      The name of the invoked service candidate
     */
    public ServiceCandidateEntryEvent(long traceId, long timestamp, Location location, String name) {
        this(traceId, timestamp, location, name, false, null);
    }

    @Override
    public int hashCode() {
        return (int) (this.traceId() + this.timestamp());
    }

}
