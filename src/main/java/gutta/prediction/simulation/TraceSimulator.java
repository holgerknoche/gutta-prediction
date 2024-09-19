package gutta.prediction.simulation;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.MonitoringEvent;

import java.util.ArrayList;
import java.util.List;

public class TraceSimulator {
    
    private final List<TraceSimulationListener> listeners;
    
    private final DeploymentModel deploymentModel;
        
    public TraceSimulator(DeploymentModel deploymentModel) {
        this.deploymentModel = deploymentModel;
        this.listeners = new ArrayList<>();
    }
    
    public TraceSimulator addListener(TraceSimulationListener listener) {
        this.listeners.add(listener);
        return this;
    }
    
    public void processEvents(List<MonitoringEvent> events) {
        new TraceSimulatorWorker(List.copyOf(this.listeners), events, this.deploymentModel).processEvents();
    }
    
}
