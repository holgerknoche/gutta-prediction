package gutta.prediction.analysis.overhead;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;
import gutta.prediction.rewriting.OverheadRewriter;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.inference.TTest;

import java.util.Collection;

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

        var traceIndex = 0;
        for (var trace : traces) {
            var originalTraceResult = this.analyzeTrace(trace, deploymentModel);

            var rewrittenTrace = new OverheadRewriter(scenarioModel).rewriteTrace(trace);
            var rewrittenTraceResult = this.analyzeTrace(rewrittenTrace, scenarioModel);

            originalDurations[traceIndex] = originalTraceResult.duration();
            scenarioDurations[traceIndex] = rewrittenTraceResult.duration();

            originalSumOfRemoteCalls += originalTraceResult.numberOfRemoteCalls();
            scenarioSumOfRemoteCalls += rewrittenTraceResult.numberOfRemoteCalls();

            traceIndex++;
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

    /**
     * This class represents the result of a {@link DurationChangeAnalysis}.
     * 
     * 
     */
    public record Result(boolean significantChange, double pValue, double originalMean, double modifiedMean, double oldAverageNumberOfRemoteCalls,
            double newAverageNumberOfRemoteCalls) {
    }

}
