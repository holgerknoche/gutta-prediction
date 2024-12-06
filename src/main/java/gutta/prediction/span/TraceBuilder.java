package gutta.prediction.span;

import gutta.prediction.analysis.consistency.ConsistencyIssue;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;

import java.util.Set;

/**
 * A {@link TraceBuilder} builds a span trace from an event trace, including trace overlays.
 */
public class TraceBuilder {

    /**
     * Builds a span trace from the given event trace.
     * 
     * @param eventTrace        The event trace to build a span trace from
     * @param deploymentModel   The deployment model to use for the simulation to determine the trace overlays
     * @param consistencyIssues A set of consistency issues to show in the trace
     * @return The built span trace
     */
    public Trace buildTrace(EventTrace eventTrace, DeploymentModel deploymentModel, Set<ConsistencyIssue<?>> consistencyIssues) {
        return new TraceBuilderWorker().buildTrace(eventTrace, deploymentModel, consistencyIssues);
    }

}
