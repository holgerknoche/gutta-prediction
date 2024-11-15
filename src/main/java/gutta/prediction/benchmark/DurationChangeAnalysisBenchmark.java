package gutta.prediction.benchmark;

import gutta.prediction.analysis.overhead.DurationChangeAnalysis;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;

import java.util.Collection;

public class DurationChangeAnalysisBenchmark extends AnalysisSpeedBenchmark {
    
    public static void main(String[] arguments) {
        new DurationChangeAnalysisBenchmark().runBenchmark(arguments);
    }
    
    @Override
    protected void runAnalysis(Collection<EventTrace> traces, DeploymentModel deploymentModel, DeploymentModel scenarioModel) {
        new DurationChangeAnalysis().analyzeTraces(traces, deploymentModel, scenarioModel, 0.05);
    }

}
