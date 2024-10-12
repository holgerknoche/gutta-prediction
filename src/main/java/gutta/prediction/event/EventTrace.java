package gutta.prediction.event;

import gutta.prediction.util.EqualityUtil;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class EventTrace {
    
    private final List<MonitoringEvent> events;
    
    public static EventTrace of(MonitoringEvent... events) {
        return new EventTrace(List.of(events));
    }
    
    public static EventTrace of(List<MonitoringEvent> events) {
        return new EventTrace(List.copyOf(events));
    }
    
    protected EventTrace(List<MonitoringEvent> events) {
        this.events = requireNonNull(events);
    }
    
    public int size() {
        return this.events.size();
    }
    
    public List<MonitoringEvent> events() {
        return Collections.unmodifiableList(this.events);
    }
    
    public void forEach(Consumer<MonitoringEvent> action) {
        this.events.forEach(action);
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
