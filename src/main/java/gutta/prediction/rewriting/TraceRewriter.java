package gutta.prediction.rewriting;

import gutta.prediction.event.EventTrace;

public interface TraceRewriter {
    
    EventTrace rewriteTrace(EventTrace inputTrace);

}
