package gutta.prediction.rewriting;

import gutta.prediction.event.MonitoringEvent;

class SimpleRewrittenEventCollector extends RewrittenEventCollector {
    
    public SimpleRewrittenEventCollector(int expectedSize) {
        super(expectedSize);
    }

    @Override
    public void addRewrittenEvent(MonitoringEvent rewrittenEvent, MonitoringEvent originalEvent) {
        this.addEventAndCorrespondence(rewrittenEvent, originalEvent);
    }

}
