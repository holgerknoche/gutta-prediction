package gutta.prediction.event;

public record ServiceCandidateExitEvent(long traceId, long timestamp, Location location, String name) implements ServiceCandidateEvent {

}
