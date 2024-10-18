package gutta.prediction.event;

public record UseCaseEndEvent(long traceId, long timestamp, Location location, String name) implements UseCaseEvent {

}
