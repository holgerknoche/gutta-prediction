package gutta.prediction.rewriting;

import gutta.prediction.event.MonitoringEvent;

import java.util.function.Function;

/**
 * Specific {@link RewrittenEventCollector} that joins the rewritten element map with an existing one.
 * This is collector is used if a trace is rewritten multiple times.
 */
class JoiningRewrittenEventCollector extends RewrittenEventCollector {

    private final Function<MonitoringEvent, MonitoringEvent> existingEventMap;
    
    public JoiningRewrittenEventCollector(int expectedSize, Function<MonitoringEvent, MonitoringEvent> existingEventMap) {
        super(expectedSize);
        
        this.existingEventMap = existingEventMap;
    }
    
    @Override
    public void addRewrittenEvent(MonitoringEvent rewrittenEvent, MonitoringEvent originalEvent) {
        var actualOriginalEvent = this.existingEventMap.apply(originalEvent);
        this.addEventAndCorrespondence(rewrittenEvent, actualOriginalEvent);        
    }

}
