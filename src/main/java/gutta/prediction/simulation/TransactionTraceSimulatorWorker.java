package gutta.prediction.simulation;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;

import java.util.List;

class TransactionTraceSimulatorWorker extends BasicTraceSimulatorWorker {

    public TransactionTraceSimulatorWorker(List<TraceSimulationListener> listeners, EventTrace trace, DeploymentModel deploymentModel) {
        super(listeners, trace, deploymentModel);
    }
    
}
