package gutta.prediction.rewriting;

import gutta.prediction.event.MonitoringEvent;

import java.util.List;

public interface TraceRewriter {
    
    List<MonitoringEvent> rewriteTrace(List<MonitoringEvent> inputTrace);

}
