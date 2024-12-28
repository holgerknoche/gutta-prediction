package gutta.prediction.analysis.overhead;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;
import gutta.prediction.rewriting.OverheadRewriter;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.inference.TTest;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Collectors;

/**
 * A {@link DurationChangeAnalysis} performs an analysis of the duration change of a collection of event traces caused by a scenario. For this purpose, all
 * traces are rewritten, and the durations are compared using a significance test.
 */
public class DurationChangeAnalysis {

    /**
     * Analyzes the given trace with respect to the given scenario.
     * 
     * @param traces            The traces to analyze
     * @param deploymentModel   The deployment model of the given trace
     * @param scenarioModel     The scenario model based on the given deployment model
     * @param significanceLevel The desired significance level for the significance test
     * @return The result of the analysis
     */
    public Result analyzeTraces(Collection<EventTrace> traces, DeploymentModel deploymentModel, DeploymentModel scenarioModel, double significanceLevel) {
        var numberOfTraces = traces.size();

        var originalDurations = new double[numberOfTraces];
        var scenarioDurations = new double[numberOfTraces];

        var originalSumOfRemoteCalls = 0;
        var scenarioSumOfRemoteCalls = 0;
        
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Enqueue the analysis tasks for the original traces
            var originalTraceSubtasks = traces.stream()
                    .map(trace -> scope.fork(() -> this.analyzeTrace(trace, deploymentModel)))
                    .collect(Collectors.toList());
            
            // Enqueue the analysis tasks for the scenario traces
            var rewrittenTraceSubtasks = traces.stream()
                    .map(trace -> scope.fork(() -> this.rewriteAndAnalyzeTrace(trace, scenarioModel)))
                    .collect(Collectors.toList());
            
            // Run the tasks
            scope.join().throwIfFailed();
            
            // Collect the results
            for (var traceIndex = 0; traceIndex < numberOfTraces; traceIndex++) {
                var originalTraceResult = originalTraceSubtasks.get(traceIndex).get();
                var rewrittenTraceResult = rewrittenTraceSubtasks.get(traceIndex).get();

                originalDurations[traceIndex] = originalTraceResult.duration();
                scenarioDurations[traceIndex] = rewrittenTraceResult.duration();

                originalSumOfRemoteCalls += originalTraceResult.numberOfRemoteCalls();
                scenarioSumOfRemoteCalls += rewrittenTraceResult.numberOfRemoteCalls();                
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DurationChangeAnalysisException("Unexpected interrupt during the analysis.", e);
        } catch (ExecutionException e) {
            var exceptionToReport = (e.getCause() != null) ? e.getCause() : e;
            throw new DurationChangeAnalysisException("Execution exception during the analysis.", exceptionToReport);
        }
        
        // Perform a heteroscedastic t-Test for the durations
        var pValue = (traces.size() < 2) ? Double.NaN : new TTest().tTest(originalDurations, scenarioDurations);
        var originalMean = StatUtils.mean(originalDurations);
        var modifiedMean = StatUtils.mean(scenarioDurations);
        var significantChange = (pValue <= significanceLevel);

        // Calculate averages for remote calls
        var originalAverageNumberOfRemoteCalls = (double) originalSumOfRemoteCalls / (double) traces.size();
        var modifiedAverageNumberOfRemoteCalls = (double) scenarioSumOfRemoteCalls / (double) traces.size();

        return new Result(significantChange, pValue, originalMean, modifiedMean, originalAverageNumberOfRemoteCalls, modifiedAverageNumberOfRemoteCalls);
    }

    private OverheadAnalyzer.Result analyzeTrace(EventTrace trace, DeploymentModel deploymentModel) {
        return new OverheadAnalyzer().analyzeTrace(trace, deploymentModel);
    }
    
    private OverheadAnalyzer.Result rewriteAndAnalyzeTrace(EventTrace originalTrace, DeploymentModel scenarioModel) {
        var rewrittenTrace = new OverheadRewriter(scenarioModel).rewriteTrace(originalTrace);
        return this.analyzeTrace(rewrittenTrace, scenarioModel);        
    }
    
    /**
     * This class represents the result of a {@link DurationChangeAnalysis}.
     * 
     * 
     */
    public record Result(boolean significantChange, double pValue, double originalMean, double modifiedMean, double oldAverageNumberOfRemoteCalls,
            double newAverageNumberOfRemoteCalls) {
    }
    
    /**
     * This exception is thrown when an error occurs during a {@link DurationChangeAnalysis}.
     */
    static class DurationChangeAnalysisException extends RuntimeException {
        
        private static final long serialVersionUID = 6140429524214282426L;

        public DurationChangeAnalysisException(String message, Throwable cause) {
            super(message, cause);
        }
        
    }

}
