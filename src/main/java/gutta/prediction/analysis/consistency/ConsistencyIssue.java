package gutta.prediction.analysis.consistency;

import gutta.prediction.event.MonitoringEvent;

public interface ConsistencyIssue<T extends MonitoringEvent> {
    
    T event();
    
    ConsistencyIssue<T> rewriteToEvent(MonitoringEvent event);

}
