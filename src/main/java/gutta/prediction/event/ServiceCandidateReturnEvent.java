package gutta.prediction.event;

public record ServiceCandidateReturnEvent(long traceId, long timestamp, Location location, String name) implements MonitoringEvent {

}
