package gutta.prediction.benchmark;

import gutta.prediction.analysis.consistency.CheckCrossComponentAccesses;
import gutta.prediction.analysis.consistency.CheckInterleavingAccesses;
import gutta.prediction.analysis.consistency.ConsistencyIssuesAnalysis;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;

import java.util.Collection;

public class ConsistencyAnalysisSpeedBenchmark extends AnalysisSpeedBenchmark {
    
    public static void main(String[] arguments) {
        new ConsistencyAnalysisSpeedBenchmark().runBenchmark(arguments);
    }
    
    @Override
    protected void runAnalysis(Collection<EventTrace> traces, DeploymentModel deploymentModel, DeploymentModel scenarioModel) {
        for (var trace : traces) {
            new ConsistencyIssuesAnalysis(CheckCrossComponentAccesses.YES, CheckInterleavingAccesses.YES).analyzeTrace(trace, deploymentModel, scenarioModel);
        }
    }   

}
