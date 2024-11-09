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
        
        for (var trace : traces) {
            var originalDuration = this.determineDuration(trace, originalDeploymentModel);
            
            var modifiedTrace = new OverheadRewriter(modifiedDeploymentModel).rewriteTrace(trace);
            var modifiedDuration = this.determineDuration(modifiedTrace, modifiedDeploymentModel);
            
            originalDurationsList.add(originalDuration);
            modifiedDurationsList.add(modifiedDuration);
        }
        
        var originalDurations = toDoubleArray(originalDurationsList);
        var modifiedDurations = toDoubleArray(modifiedDurationsList);
        
        // Perform a heteroscedastic t-Test for the durations
        var pValue = (traces.size() < 2) ? 1.0 : new TTest().tTest(originalDurations, modifiedDurations); 
        var originalMean = StatUtils.mean(originalDurations);
        var modifiedMean = StatUtils.mean(modifiedDurations); 
        
        return new Result((pValue <= significanceLevel), pValue, originalMean, modifiedMean);
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

    private double determineDuration(EventTrace trace, DeploymentModel deploymentModel) {
        var analyzerResult = new OverheadAnalyzer().analyzeTrace(trace, deploymentModel);
        return analyzerResult.duration();
    }
    
    public record Result(boolean significantChange, double pValue, double originalMean, double modifiedMean) {}

}
