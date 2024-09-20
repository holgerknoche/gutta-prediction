package gutta.prediction.event;

import gutta.prediction.util.EqualityUtil;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class EventTrace {
    
    private final List<MonitoringEvent> events;
    
    public static EventTrace of(MonitoringEvent... events) {
        return new EventTrace(List.of(events));
    }
    
    public EventTrace(List<MonitoringEvent> events) {
        this.events = requireNonNull(events);
    }
    
    public List<MonitoringEvent> events() {
        return Collections.unmodifiableList(this.events);
    }
    
    @Override
    public int hashCode() {
        return this.events.hashCode();
    }
    
    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    private boolean equalsInternal(EventTrace that) {
        return this.events.equals(that.events);
    }

}
