package gutta.prediction.event;

public interface MonitoringEvent {
	
	long traceId();
	
	long timestamp();
	
	Location location();
	
	<R> R accept(MonitoringEventVisitor<R> visitor);
	
}
