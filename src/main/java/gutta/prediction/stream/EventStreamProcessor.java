package gutta.prediction.stream;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.MonitoringEvent;

import java.util.ArrayList;
import java.util.List;

public class EventStreamProcessor {
    
    private final List<EventStreamProcessorListener> listeners;
    
    private final DeploymentModel originalDeploymentModel;
    
    private final DeploymentModel modifiedDeploymentModel;
    
    public EventStreamProcessor(DeploymentModel originalDeploymentModel, DeploymentModel modifiedDeploymentModel) {
        this.originalDeploymentModel = originalDeploymentModel;
        this.modifiedDeploymentModel = modifiedDeploymentModel;
        this.listeners = new ArrayList<>();
    }
    
    public EventStreamProcessor addListener(EventStreamProcessorListener listener) {
        this.listeners.add(listener);
        return this;
    }
    
    public void processEvents(List<MonitoringEvent> events) {
        new EventStreamProcessorWorker(List.copyOf(this.listeners), events, this.originalDeploymentModel, this.modifiedDeploymentModel).processEvents();
    }
    
}
