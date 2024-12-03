package gutta.prediction.event;

import gutta.prediction.util.EqualityUtil;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * An {@link EventTrace} represents a trace (i.e., sequence) of monitoring events.
 */
public class EventTrace {

    private final List<MonitoringEvent> events;

    /**
     * Creates a trace consisting of the given events.
     * 
     * @param events The events to store in the trace
     * @return The (possibly empty) trace
     */
    public static EventTrace of(MonitoringEvent... events) {
        return new EventTrace(List.of(events));
    }

    /**
     * Creates a trace consisting of the given events.
     * 
     * @param events The events to store in the trace
     * @return The (possibly empty) trace
     */
    public static EventTrace of(List<MonitoringEvent> events) {
        return new EventTrace(List.copyOf(events));
    }

    /**
     * Creates a trace consisting of the given events.
     * 
     * @param events The events to store in the trace
     */
    protected EventTrace(List<MonitoringEvent> events) {
        this.events = requireNonNull(events);
    }

    /**
     * Returns the number of events in this trace.
     * 
     * @return see above
     */
    public int size() {
        return this.events.size();
    }

    /**
     * Returns the events contained in this trace.
     * 
     * @return see above. The list is unmodifiable.
     */
    public List<MonitoringEvent> events() {
        return Collections.unmodifiableList(this.events);
    }

    /**
     * Performs the given action for every event in the trace.
     * 
     * @param action The action to perform
     */
    public void forEach(Consumer<MonitoringEvent> action) {
        this.events.forEach(action);
    }

    /**
     * Returns the trace ID of this trace. The ID is derived from the first event.
     * 
     * @return see above, 0 for an empty trace
     */
    public long traceId() {
        if (this.events().isEmpty()) {
            return 0;
        } else {
            return this.events().get(0).traceId();
        }
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

    @Override
    public String toString() {
        return this.events.toString();
    }

}
