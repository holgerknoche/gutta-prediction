package gutta.prediction.simulation;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;

import java.util.ArrayList;
import java.util.List;

public class TraceSimulator {
    
    private final List<TraceSimulationListener> listeners;
    
    private final DeploymentModel deploymentModel;

    public static void runSimulationOf(EventTrace trace, DeploymentModel deploymentModel, TraceSimulationMode mode, TraceSimulationListener listener) {
        new TraceSimulator(deploymentModel)
            .addListener(listener)
            .processEvents(trace, mode);
    }
    
    public TraceSimulator(DeploymentModel deploymentModel) {
        this.deploymentModel = deploymentModel;
        this.listeners = new ArrayList<>();
    }
    
    public TraceSimulator addListener(TraceSimulationListener listener) {
        this.listeners.add(listener);
        return this;
    }
    
    public void processEvents(EventTrace trace, TraceSimulationMode mode) {
        WorkerCreator workerCreator = switch (mode) {
        case BASIC -> BasicTraceSimulatorWorker::new;
        case WITH_TRANSACTIONS -> TransactionTraceSimulatorWorker::new;
        case WITH_ENTITY_ACCESS -> EntityAccessSimulatorWorker::new;        
        };
        
        var worker = workerCreator.createWorker(List.copyOf(this.listeners), trace, this.deploymentModel);
        worker.processEvents();
    }
    
    private interface WorkerCreator {
        
        TraceSimulatorWorker createWorker(List<TraceSimulationListener> listeners, EventTrace trace, DeploymentModel deploymentModel);
        
    }
    
}
