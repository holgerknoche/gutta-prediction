package gutta.prediction.event;

/**
 * An {ServiceCandidateEvent} is a supertype for all events that relate to a service candidate.
 */
public sealed interface ServiceCandidateEvent extends MonitoringEvent
        permits ServiceCandidateEntryEvent, ServiceCandidateInvocationEvent, ServiceCandidateExitEvent, ServiceCandidateReturnEvent {

    /**
     * Returns the name of the affected service candidate.
     * 
     * @return see above
     */
    String name();

}
