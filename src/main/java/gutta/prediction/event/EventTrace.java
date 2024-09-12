package gutta.prediction.event;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class EventTrace implements Iterable<MonitoringEvent> {
	
	private final List<MonitoringEvent> events;
	
	public EventTrace(List<MonitoringEvent> events) {
		this.events = events;
	}
	
	public boolean isEmpty() {
		return this.events.isEmpty();
	}
	
	public Optional<MonitoringEvent> firstEvent() {
		return (this.isEmpty()) ? Optional.empty() : Optional.of(this.events.get(0)); 
	}
	
	@Override
	public Iterator<MonitoringEvent> iterator() {
		return this.events.iterator();
	}

}
