package gutta.prediction.rewriting;

import gutta.prediction.event.MonitoringEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Supertype for objects that collect the rewritten events during the rewriting process.
 */
abstract class RewrittenEventCollector {

    private final List<MonitoringEvent> rewrittenEvents;
    
    private final Map<MonitoringEvent, MonitoringEvent> eventCorrespondence;
    
    protected RewrittenEventCollector(int expectedSize) {
        this.rewrittenEvents = new ArrayList<>(expectedSize);
        this.eventCorrespondence = new HashMap<>(expectedSize);
    }
    
    public abstract void addRewrittenEvent(MonitoringEvent rewrittenEvent, MonitoringEvent originalEvent);
    
    protected void addEventAndCorrespondence(MonitoringEvent rewrittenEvent, MonitoringEvent originalEvent) {
        this.rewrittenEvents.add(rewrittenEvent);
        this.eventCorrespondence.put(rewrittenEvent, originalEvent);        
    }
    
    public RewrittenEventTrace createTrace() {
        return new RewrittenEventTrace(this.rewrittenEvents, this.eventCorrespondence);
    }
    
}
