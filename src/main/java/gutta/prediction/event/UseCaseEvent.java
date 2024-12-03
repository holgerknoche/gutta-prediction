package gutta.prediction.event;

/**
 * A {@link UseCaseEvent} is a supertype for all events related to a use case, such as the start of a use case.
 */
public sealed interface UseCaseEvent extends MonitoringEvent permits UseCaseStartEvent, UseCaseEndEvent {
    
    /** 
     * Returns the name of the use case affected by this event.
     * 
     * @return see above
     */
    String name();

}
