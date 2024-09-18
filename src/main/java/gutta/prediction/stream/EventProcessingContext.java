package gutta.prediction.stream;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;

import java.util.ArrayDeque;
import java.util.Deque;

public class EventProcessingContext {

    private final DeploymentModel originalDeploymentModel;
    
    private final DeploymentModel modifiedDeploymentModel;
    
    private final EventStream eventStream;
    
    private Deque<StackEntry> stack = new ArrayDeque<>();

    private ServiceCandidate currentServiceCandidate;

    private Component currentComponent;

    private Location currentLocation;
    
    EventProcessingContext(DeploymentModel originalDeploymentModel, DeploymentModel modifiedDeploymentModel, EventStream eventStream) {
        this.originalDeploymentModel = originalDeploymentModel;
        this.modifiedDeploymentModel = modifiedDeploymentModel;
        this.eventStream = eventStream;
    }
    
    public ServiceCandidate currentServiceCandidate() {
        return this.currentServiceCandidate;
    }
    
    void currentServiceCandidate(ServiceCandidate candidate) {
        this.currentServiceCandidate = candidate;
    }
    
    public Component currentComponent() {
        return this.currentComponent;
    }
    
    void currentComponent(Component component) {
        this.currentComponent = component;
    }
    
    public Location currentLocation() {
        return this.currentLocation;
    }
    
    void currentLocation(Location location) {
        this.currentLocation = location;
    }
    
    public DeploymentModel originalDeploymentModel() {
        return this.originalDeploymentModel;
    }
    
    public DeploymentModel modifiedDeploymentModel() {
        return this.modifiedDeploymentModel;
    }
    
    public MonitoringEvent lookahead(int amount) {
        return this.eventStream.lookahead(amount);
    }
    
    public MonitoringEvent lookback(int amount) {
        return this.eventStream.lookback(amount);
    }
    
    public StackEntry peek() {
        return this.stack.peek();
    }
    
    void pushCurrentState() {
        this.stack.push(new StackEntry(this.currentServiceCandidate, this.currentComponent, this.currentLocation));
    }
    
    StackEntry popCurrentState() {
        var entry = this.stack.pop();
        
        this.currentServiceCandidate = entry.serviceCandidate();
        this.currentComponent = entry.component();
        this.currentLocation = entry.location();
        
        return entry;
    }
        
}
