package gutta.prediction.rewriting;

import gutta.prediction.event.MonitoringEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class RewrittenEventCollector {

    private final List<MonitoringEvent> rewrittenEvents = new ArrayList<>();
    
    private final Map<MonitoringEvent, MonitoringEvent> eventCorrespondence = new HashMap<>();
    
    public abstract void addRewrittenEvent(MonitoringEvent rewrittenEvent, MonitoringEvent originalEvent);
    
    protected void addEventAndCorrespondence(MonitoringEvent rewrittenEvent, MonitoringEvent originalEvent) {
        this.rewrittenEvents.add(rewrittenEvent);
        this.eventCorrespondence.put(rewrittenEvent, originalEvent);        
    }
    
    public RewrittenEventTrace createTrace() {
        return new RewrittenEventTrace(this.rewrittenEvents, this.eventCorrespondence);
    }
    
}
