package gutta.prediction.rewriting;

import gutta.prediction.event.EventTrace;

/**
 * Common interface for all trace rewriters.
 */
public interface TraceRewriter {

    /**
     * Rewrites the given event trace.
     * 
     * @param inputTrace The event trace to rewrite
     * @return The rewritten event trace
     */
    RewrittenEventTrace rewriteTrace(EventTrace inputTrace);

}
