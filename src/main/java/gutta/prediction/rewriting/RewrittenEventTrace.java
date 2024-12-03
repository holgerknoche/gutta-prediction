package gutta.prediction.rewriting;

import gutta.prediction.event.EventTrace;
import gutta.prediction.event.MonitoringEvent;

import java.util.List;
import java.util.Map;

/**
 * A {@link RewrittenEventTrace} is a special event trace that contains an event map in addition to the actual events. This allows to map a rewritten event to
 * its original event.
 */
public class RewrittenEventTrace extends EventTrace {

    private final Map<MonitoringEvent, MonitoringEvent> eventMap;

    /**
     * Creates a new rewritten event trace from the given data.
     * 
     * @param events   The events in the trace
     * @param eventMap The map of the events in the trace to the original events prior to rewriting
     */
    public RewrittenEventTrace(List<MonitoringEvent> events, Map<MonitoringEvent, MonitoringEvent> eventMap) {
        super(events);

        this.eventMap = eventMap;
    }

    /**
     * Obtains the original event for the given rewritten event from this trace.
     * 
     * @param <T>            The type of the event
     * @param rewrittenEvent The rewritten event to obtain the original event for
     * @return The original event
     */
    @SuppressWarnings("unchecked")
    public <T extends MonitoringEvent> T obtainOriginalEvent(T rewrittenEvent) {
        return (T) this.eventMap.get(rewrittenEvent);
    }

}
