package gutta.prediction.analysis;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;

public class LatencyChangeAnalysis {

    public void analyzeTrace(EventTrace trace, DeploymentModel originalDeploymentModel, DeploymentModel modifiedDeploymentModel) {
        var originalData = new LatencyAnalyzer().analyzeTrace(trace, originalDeploymentModel);
        var modifiedData = new LatencyAnalyzer().analyzeTrace(trace, modifiedDeploymentModel);
    }
        
}
