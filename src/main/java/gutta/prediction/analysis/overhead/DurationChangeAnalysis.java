package gutta.prediction.analysis.overhead;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;
import gutta.prediction.rewriting.OverheadRewriter;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.inference.TTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DurationChangeAnalysis {

    public Result analyzeTraces(Collection<EventTrace> traces, DeploymentModel originalDeploymentModel, DeploymentModel modifiedDeploymentModel, double significanceLevel) {
        var originalDurationsList = new ArrayList<Double>();
        var modifiedDurationsList = new ArrayList<Double>();
        
        var originalSumOfRemoteCalls = 0L;
        var modifiedSumOfRemoteCalls = 0L;
        
        for (var trace : traces) {
            var originalTraceResult = this.analyzeTrace(trace, originalDeploymentModel);
            
            var modifiedTrace = new OverheadRewriter(modifiedDeploymentModel).rewriteTrace(trace);
            var modifiedTraceResult = this.analyzeTrace(modifiedTrace, modifiedDeploymentModel);
            
            originalDurationsList.add((double) originalTraceResult.duration());
            modifiedDurationsList.add((double) modifiedTraceResult.duration());
            
            originalSumOfRemoteCalls += originalTraceResult.numberOfRemoteCalls();
            modifiedSumOfRemoteCalls += modifiedTraceResult.numberOfRemoteCalls();
        }
        
        var originalDurations = toDoubleArray(originalDurationsList);
        var modifiedDurations = toDoubleArray(modifiedDurationsList);
        
        // Perform a heteroscedastic t-Test for the durations
        var pValue = (traces.size() < 2) ? Double.NaN : new TTest().tTest(originalDurations, modifiedDurations); 
        var originalMean = StatUtils.mean(originalDurations);
        var modifiedMean = StatUtils.mean(modifiedDurations); 
        var significantChange = (pValue <= significanceLevel);
        
        // Calculate averages for remote calls
        var originalAverageNumberOfRemoteCalls = (double) originalSumOfRemoteCalls / (double) traces.size();
        var modifiedAverageNumberOfRemoteCalls = (double) modifiedSumOfRemoteCalls / (double) traces.size();
        
        return new Result(significantChange, pValue, originalMean, modifiedMean, originalAverageNumberOfRemoteCalls, modifiedAverageNumberOfRemoteCalls);
    }
    
    private static double[] toDoubleArray(List<Double> values) {
        var array = new double[values.size()];
        
        var index = 0;
        for (var value : values) {
            array[index] = value.doubleValue();
            index++;
        }
        
        return array;
    }

    private OverheadAnalyzer.Result analyzeTrace(EventTrace trace, DeploymentModel deploymentModel) {
        return new OverheadAnalyzer().analyzeTrace(trace, deploymentModel);        
    }
    
    public record Result(boolean significantChange, double pValue, double originalMean, double modifiedMean, double oldAverageNumberOfRemoteCalls, double newAverageNumberOfRemoteCalls) {}

}
