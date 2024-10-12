package gutta.prediction.event;

/**
 * A {@link MonitoringEvent} represents a meaningful event in a observed or rewritten {@link EventTrace}.
 */
public sealed interface MonitoringEvent permits EntityReadEvent, EntityWriteEvent, ExplicitTransactionAbortEvent, ImplicitTransactionAbortEvent,
        ServiceCandidateEntryEvent, ServiceCandidateExitEvent, ServiceCandidateInvocationEvent, ServiceCandidateReturnEvent, TransactionCommitEvent,
        TransactionStartEvent, UseCaseEndEvent, UseCaseStartEvent {

    /**
     * The ID of the trace in which this event occurred.
     * 
     * @return see above
     */
    long traceId();

    /**
     * The timestamp (in nanoseconds) at which this event occurred.
     * 
     * @return see above
     */
    long timestamp();

    /**
     * The location at which this event occurred.
     * 
     * @return see above
     */
    Location location();

    /**
     * Accepts the given visitor.
     * 
     * @param <R>     The type of the operation provided by the visitor
     * @param visitor The visitor to accept
     * @return The result of the operation provided by the visitor
     */
    <R> R accept(MonitoringEventVisitor<R> visitor);

}
