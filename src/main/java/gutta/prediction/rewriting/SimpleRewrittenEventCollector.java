package gutta.prediction.rewriting;

import gutta.prediction.event.MonitoringEvent;

class SimpleRewrittenEventCollector extends RewrittenEventCollector {

    @Override
    public void addRewrittenEvent(MonitoringEvent rewrittenEvent, MonitoringEvent originalEvent) {
        this.addEventAndCorrespondence(rewrittenEvent, originalEvent);
    }

}
