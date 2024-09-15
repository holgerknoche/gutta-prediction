package gutta.prediction.common;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnectionProperties;
import gutta.prediction.domain.ComponentConnections;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.MonitoringEventVisitor;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.util.EventStream;

import java.util.List;
import java.util.Map;

public abstract class AbstractMonitoringEventProcessor implements MonitoringEventVisitor<Void> {
    
    private final EventStream events;

    private final Map<String, Component> useCaseAllocation;

    private final Map<String, Component> methodAllocation;

    private final ComponentConnections connections;

    protected AbstractMonitoringEventProcessor(List<MonitoringEvent> events, Map<String, Component> useCaseAllocation, Map<String, Component> methodAllocation, ComponentConnections connections) {
        this.events = new EventStream(events);
        this.useCaseAllocation = useCaseAllocation;
        this.methodAllocation = methodAllocation;
        this.connections = connections;
    }

    protected Component determineComponentForUseCase(String useCaseName) {
        var component = this.useCaseAllocation.get(useCaseName);
        if (component == null) {
            throw new IllegalStateException("Use case '" + useCaseName + "' is not assigned to a component.");
        }
        
        return component;
    }
    
    protected Component determineComponentForServiceCandidate(String candidateName) {
        var component = this.methodAllocation.get(candidateName);
        if (component == null) {
            throw new IllegalStateException("Service candidate '" + candidateName + "' is not assigned to a component.");
        }
        
        return component;
    }
    
    protected ComponentConnectionProperties determineConnectionBetween(Component sourceComponent, Component targetComponent) {
        return this.connections.getConnection(sourceComponent, targetComponent)
                .orElseThrow(() -> new IllegalStateException("No connection from '" + sourceComponent + "' to '" + targetComponent + "'."));
    }
    
    protected void processEvents() {
        while (true) {
            var currentEvent = this.events.lookahead(0);
            if (currentEvent == null) {
                break;
            }
            
            currentEvent.accept(this);
            this.events.consume();
        }
    }
    
    protected MonitoringEvent lookahead(int amount) {
        return this.events.lookahead(amount);
    }
    
    protected void processCandidateInvocation(Component sourceComponent, ServiceCandidateInvocationEvent invocationEvent, ActionOnCandidateInvocation action) {
        var nextEvent = this.lookahead(1);
        if (nextEvent instanceof ServiceCandidateEntryEvent entryEvent) {
            var targetComponent = this.determineComponentForServiceCandidate(entryEvent.name());                
            var connection = this.determineConnectionBetween(sourceComponent, targetComponent);

            action.perform(invocationEvent, entryEvent, sourceComponent, connection);
        } else {
            throw new IllegalStateException("A service candidate invocation event is not followed by a service candidate entry event.");
        }
    }
        
    protected interface ActionOnCandidateInvocation {
        
        void perform(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, Component targetComponent, ComponentConnectionProperties connection);
        
    }
    
}
