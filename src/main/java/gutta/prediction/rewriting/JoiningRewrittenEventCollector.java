package gutta.prediction.rewriting;

import gutta.prediction.event.MonitoringEvent;

import java.util.function.Function;

class JoiningRewrittenEventCollector extends RewrittenEventCollector {

    private final Function<MonitoringEvent, MonitoringEvent> existingCorrespondence;
    
    public JoiningRewrittenEventCollector(Function<MonitoringEvent, MonitoringEvent> existingCorrespondence) {
        this.existingCorrespondence = existingCorrespondence;
    }
    
    @Override
    public void addRewrittenEvent(MonitoringEvent rewrittenEvent, MonitoringEvent originalEvent) {
        var actualOriginalEvent = this.existingCorrespondence.apply(originalEvent);
        this.addEventAndCorrespondence(rewrittenEvent, actualOriginalEvent);        
    }

}
