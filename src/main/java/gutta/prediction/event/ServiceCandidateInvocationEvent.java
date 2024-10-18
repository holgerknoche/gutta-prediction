package gutta.prediction.event;

public record ServiceCandidateInvocationEvent(long traceId, long timestamp, Location location, String name) implements ServiceCandidateEvent {

}
