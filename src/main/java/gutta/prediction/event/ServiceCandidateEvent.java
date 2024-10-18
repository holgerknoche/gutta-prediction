package gutta.prediction.event;

public sealed interface ServiceCandidateEvent extends MonitoringEvent
        permits ServiceCandidateEntryEvent, ServiceCandidateInvocationEvent, ServiceCandidateExitEvent, ServiceCandidateReturnEvent {

    String name();

}
