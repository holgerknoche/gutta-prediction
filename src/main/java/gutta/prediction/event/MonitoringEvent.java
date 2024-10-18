package gutta.prediction.event;

/**
 * A {@link MonitoringEvent} represents a meaningful event in a observed or rewritten {@link EventTrace}.
 */
public sealed interface MonitoringEvent permits EntityAccessEvent, ServiceCandidateEvent, TransactionEvent, UseCaseEvent {

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

}
