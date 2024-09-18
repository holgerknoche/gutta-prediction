package gutta.prediction.stream;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.MonitoringEvent;

import java.util.ArrayList;
import java.util.List;

public class EventStreamProcessor {
    
    private final List<EventStreamProcessorListener> listeners;
    
    private final DeploymentModel deploymentModel;
        
    public EventStreamProcessor(DeploymentModel deploymentModel) {
        this.deploymentModel = deploymentModel;
        this.listeners = new ArrayList<>();
    }
    
    public EventStreamProcessor addListener(EventStreamProcessorListener listener) {
        this.listeners.add(listener);
        return this;
    }
    
    public void processEvents(List<MonitoringEvent> events) {
        new EventStreamProcessorWorker(List.copyOf(this.listeners), events, this.deploymentModel).processEvents();
    }
    
}
