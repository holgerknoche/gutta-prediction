package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;
import gutta.prediction.rewriting.LatencyRewriter;
import gutta.prediction.rewriting.TransactionContextRewriter;

public class ConsistencyIssuesAnalysis {
    
    public void analyzeTrace(EventTrace trace, DeploymentModel originalDeploymentModel, DeploymentModel modifiedDeploymentModel) {        
        var originalTraceResult = new ConsistencyIssuesAnalyzer().analyzeTrace(trace, originalDeploymentModel);
        
        var rewrittenTrace = this.rewriteTrace(trace, modifiedDeploymentModel);
        var rewrittenTraceResult = new ConsistencyIssuesAnalyzer().analyzeTrace(rewrittenTrace, modifiedDeploymentModel);
        
        // TODO Diff the results
        originalTraceResult.diff(rewrittenTraceResult);
    }
    
    private EventTrace rewriteTrace(EventTrace trace, DeploymentModel modifiedDeploymentModel) {
        var latencyRewriter = new LatencyRewriter(modifiedDeploymentModel);
        var transactionRewriter = new TransactionContextRewriter(modifiedDeploymentModel);
        return transactionRewriter.rewriteTrace(latencyRewriter.rewriteTrace(trace));
    }

}
