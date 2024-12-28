package gutta.prediction.benchmark;

import gutta.prediction.analysis.consistency.CheckCrossComponentAccesses;
import gutta.prediction.analysis.consistency.CheckInterleavingAccesses;
import gutta.prediction.analysis.consistency.ConsistencyIssuesAnalysis;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

/**
 * A benchmark that measures the speed of the consistency analysis using given data.
 */
public class ConsistencyAnalysisSpeedBenchmark extends AnalysisSpeedBenchmark {

    /**
     * Runs the benchmark.
     * 
     * @param arguments The command line arguments of the benchmark
     */
    public static void main(String[] arguments) {
        new ConsistencyAnalysisSpeedBenchmark().runBenchmark(arguments);
    }

    @Override
    protected void runAnalysis(Collection<EventTrace> traces, DeploymentModel deploymentModel, DeploymentModel scenarioModel) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            traces.forEach(trace -> scope.fork(() -> {
                var analysis = new ConsistencyIssuesAnalysis(CheckCrossComponentAccesses.YES, CheckInterleavingAccesses.YES);
                return analysis.analyzeTrace(trace, deploymentModel, scenarioModel);
            }));

            scope.join().throwIfFailed();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Unexpected interrupt while waiting for the analysis results.", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Execution exception during the analysis.", e);
        }

    }

}
