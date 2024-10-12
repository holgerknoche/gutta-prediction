package gutta.prediction.event;

public record UseCaseStartEvent(long traceId, long timestamp, Location location, String name) implements MonitoringEvent {

}
