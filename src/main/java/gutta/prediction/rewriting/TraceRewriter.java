package gutta.prediction.rewriting;

import gutta.prediction.event.EventTrace;

public interface TraceRewriter {
    
    RewrittenEventTrace rewriteTrace(EventTrace inputTrace);

}
