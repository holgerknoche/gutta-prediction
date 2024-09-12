package gutta.prediction.event;

public record UseCaseEndEvent(long traceId, long timestamp, Location location, String name) implements MonitoringEvent {
	
	@Override
	public <R> R accept(MonitoringEventVisitor<R> visitor) {
		return visitor.handleUseCaseEndEvent(this);
	}
	
}
