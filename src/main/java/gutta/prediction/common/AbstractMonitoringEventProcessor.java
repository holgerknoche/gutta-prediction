package gutta.prediction.common;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.MonitoringEventVisitor;
import gutta.prediction.stream.EventStream;

import java.util.List;

public abstract class AbstractMonitoringEventProcessor implements MonitoringEventVisitor<Void> {
    
    private final EventStream events;

    private final DeploymentModel originalDeploymentModel;
    
    private final DeploymentModel modifiedDeploymentModel;

    protected AbstractMonitoringEventProcessor(List<MonitoringEvent> events, DeploymentModel originalDeploymentModel, DeploymentModel modifiedDeploymentModel) {
        this.events = new EventStream(events);
        this.originalDeploymentModel = originalDeploymentModel;
        this.modifiedDeploymentModel = modifiedDeploymentModel;
    }

    protected Component determineOriginalComponentForUseCase(UseCase useCase) {
        return this.originalDeploymentModel.getComponentForUseCase(useCase).orElseThrow(() -> new IllegalArgumentException("Use case '" + useCase + "' is not assigned to a component.")); 
    }
    
    protected Component determineOriginalComponentForServiceCandidate(ServiceCandidate candidate) {
        return this.originalDeploymentModel.getComponentForServiceCandidate(candidate).orElseThrow(() -> new IllegalArgumentException("Service candidate '" + candidate + "' is not assigned to a component."));
    }
    
    protected ComponentConnection determineOriginalConnectionBetween(Component sourceComponent, Component targetComponent) {
        return this.originalDeploymentModel.getConnection(sourceComponent, targetComponent)
                .orElseThrow(() -> new IllegalStateException("No connection from '" + sourceComponent + "' to '" + targetComponent + "'."));
    }
    
    protected Component determineModifiedComponentForUseCase(UseCase useCase) {
        return this.modifiedDeploymentModel.getComponentForUseCase(useCase).orElseThrow(() -> new IllegalArgumentException("Use case '" + useCase + "' is not assigned to a component.")); 
    }
    
    protected Component determineModifiedComponentForServiceCandidate(ServiceCandidate candidate) {
        return this.modifiedDeploymentModel.getComponentForServiceCandidate(candidate).orElseThrow(() -> new IllegalArgumentException("Service candidate '" + candidate + "' is not assigned to a component."));
    }
    
    protected ComponentConnection determineModifiedConnectionBetween(Component sourceComponent, Component targetComponent) {
        return this.modifiedDeploymentModel.getConnection(sourceComponent, targetComponent)
                .orElseThrow(() -> new IllegalStateException("No connection from '" + sourceComponent + "' to '" + targetComponent + "'."));
    }
    
    protected ServiceCandidate resolveOriginalServiceCandidate(String candidateName) {
        return this.originalDeploymentModel.resolveServiceCandidateByName(candidateName).orElseThrow(() -> new IllegalArgumentException("Service candidate '" + candidateName + "' does not exist."));
    }
    
    protected ServiceCandidate resolveModifiedServiceCandidate(String candidateName) {
        return this.modifiedDeploymentModel.resolveServiceCandidateByName(candidateName).orElseThrow(() -> new IllegalArgumentException("Service candidate '" + candidateName + "' does not exist."));
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
    
    protected MonitoringEvent lookback(int amount) {
        return this.events.lookback(amount);
    }
                
}
