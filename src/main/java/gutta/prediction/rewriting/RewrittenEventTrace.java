package gutta.prediction.rewriting;

import gutta.prediction.event.EventTrace;
import gutta.prediction.event.MonitoringEvent;

import java.util.List;
import java.util.Map;

public class RewrittenEventTrace extends EventTrace {
    
    private final Map<MonitoringEvent, MonitoringEvent> eventCorrespondence;
    
    public RewrittenEventTrace(List<MonitoringEvent> events, Map<MonitoringEvent, MonitoringEvent> eventCorrespondence) {
        super(events);
        
        this.eventCorrespondence = eventCorrespondence;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends MonitoringEvent> T obtainOriginalEvent(T rewrittenEvent) {
        return (T) this.eventCorrespondence.get(rewrittenEvent);
    }

}
