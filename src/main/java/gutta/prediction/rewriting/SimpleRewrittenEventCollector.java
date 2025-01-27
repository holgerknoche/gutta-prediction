package gutta.prediction.rewriting;

import gutta.prediction.event.MonitoringEvent;

/**
 * Specific {@link RewrittenEventCollector} that builds an immediate event map during rewriting.
 * This collector is used for the first rewrite of a trace.
 */
class SimpleRewrittenEventCollector extends RewrittenEventCollector {
    
    public SimpleRewrittenEventCollector(int expectedSize) {
        super(expectedSize);
    }

    @Override
    public void addRewrittenEvent(MonitoringEvent rewrittenEvent, MonitoringEvent originalEvent) {
        this.addEventAndCorrespondence(rewrittenEvent, originalEvent);
    }

}
