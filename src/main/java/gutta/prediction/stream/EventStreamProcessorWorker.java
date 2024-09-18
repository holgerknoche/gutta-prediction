package gutta.prediction.stream;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.MonitoringEventVisitor;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionAbortEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;

import java.util.List;

class EventStreamProcessorWorker implements MonitoringEventVisitor<Void> {

    private final List<EventStreamProcessorListener> listeners;

    private final EventStream events;

    private final DeploymentModel deploymentModel;

    private final EventProcessingContext context;

    private int syntheticLocationIdCount = 0;

    public EventStreamProcessorWorker(List<EventStreamProcessorListener> listeners, List<MonitoringEvent> events, DeploymentModel deploymentModel) {
        this.listeners = listeners;
        this.events = new EventStream(events);
        this.deploymentModel = deploymentModel;
        this.context = new EventProcessingContext(deploymentModel, this.events);
    }

    public void processEvents() {
        this.onStartOfProcessing();
        this.processEventsInStream();
        this.onEndOfProcessing();
    }

    private void onStartOfProcessing() {
        // Notify listeners
        this.listeners.forEach(EventStreamProcessorListener::onStartOfProcessing);
    }

    private void processEventsInStream() {
        while (true) {
            var currentEvent = this.events.lookahead(0);
            if (currentEvent == null) {
                return;
            }

            currentEvent.accept(this);
            this.events.consume();
        }
    }

    private void onEndOfProcessing() {
        // Notify listeners
        this.listeners.forEach(EventStreamProcessorListener::onEndOfProcessing);
    }

    private SyntheticLocation createSyntheticLocation() {
        return new SyntheticLocation(this.syntheticLocationIdCount++);
    }

    @Override
    public Void handleEntityReadEvent(EntityReadEvent event) {
        this.listeners.forEach(listener -> listener.onEntityReadEvent(event, this.context));
        return null;
    }

    @Override
    public Void handleEntityWriteEvent(EntityWriteEvent event) {
        this.listeners.forEach(listener -> listener.onEntityWriteEvent(event, this.context));
        return null;
    }

    @Override
    public Void handleServiceCandidateEntryEvent(ServiceCandidateEntryEvent event) {
        this.listeners.forEach(listener -> listener.onServiceCandidateEntryEvent(event, this.context));
        return null;
    }

    @Override
    public Void handleServiceCandidateExitEvent(ServiceCandidateExitEvent event) {
        this.listeners.forEach(listener -> listener.onServiceCandidateExitEvent(event, this.context));

        var nextEvent = this.events.lookahead(1);
        if (nextEvent instanceof ServiceCandidateReturnEvent returnEvent) {
            var sourceComponent = this.context.currentComponent();
            // Determine the component to return to from the top of the stack
            var targetComponent = this.context.peek().component();
            var connection = this.deploymentModel.getConnection(sourceComponent, targetComponent)
                    .orElseThrow(() -> new TraceProcessingException(event, "No connection from '" + sourceComponent + "' to '" + targetComponent + "'."));

            this.listeners.forEach(listener -> listener.onComponentReturn(event, returnEvent, connection, this.context));
        } else {
            throw new IllegalStateException("A service candidate exit event is not followed by a service candidate return event.");
        }

        return null;
    }

    @Override
    public Void handleServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event) {
        this.listeners.forEach(listener -> listener.onServiceCandidateInvocationEvent(event, context));

        var nextEvent = this.events.lookahead(1);
        if (nextEvent instanceof ServiceCandidateEntryEvent entryEvent) {
            var invokedCandidateName = entryEvent.name();
            
            var sourceComponent = this.context.currentComponent();
            var connection = this.findConnectionForCandidateEntry(invokedCandidateName, sourceComponent, event);
            
            this.performComponentTransition(event, entryEvent, connection);
        } else {
            throw new IllegalStateException("A service candidate invocation event is not followed by a service candidate entry event.");
        }

        return null;
    }
    
    private ComponentConnection findConnectionForCandidateEntry(String candidateName, Component sourceComponent, MonitoringEvent event) {
        var invokedCandidate = this.deploymentModel.resolveServiceCandidateByName(candidateName)
                .orElseThrow(() -> new TraceProcessingException(event, "Service candidate '" + candidateName + "' does not exist."));
        var targetComponent = this.deploymentModel.getComponentForServiceCandidate(invokedCandidate)
                .orElseThrow(() -> new TraceProcessingException(event, "Service candidate '" + invokedCandidate + "' is not assigned to a component."));
        return this.deploymentModel.getConnection(sourceComponent, targetComponent)
                .orElseThrow(() -> new TraceProcessingException(event, "No connection from '" + sourceComponent + "' to '" + targetComponent + "'."));
    }
    
    private void performComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ComponentConnection connection) {
        this.listeners.forEach(listener -> listener.onComponentTransition(invocationEvent, entryEvent, connection, this.context));

        // Save the current state on the stack before making changes
        this.context.pushCurrentState();

        var targetComponent = connection.target();
        var sourceLocation = invocationEvent.location();
        var targetLocation = entryEvent.location();

        if (connection.isSynthetic()) {
            if (connection.isRemote() && !targetLocation.isSynthetic()) {
                // For transitions along synthetic remote connections, a synthetic target location is required.
                // If the target location is not already synthetic (for instance, due to a previous rewrite), it is created.
                targetLocation = this.createSyntheticLocation();
            } else if (!connection.isRemote()) {
                // If the connection is not remote, keep the current location
                targetLocation = this.context.currentLocation();
            }
        }
        
        // Ensure that the transition is valid
        this.ensureValidLocationTransition(sourceLocation, targetLocation, connection, entryEvent);
        
        var enteredCandidateName = entryEvent.name();
        var enteredServiceCandidate = this.deploymentModel.resolveServiceCandidateByName(entryEvent.name())
                .orElseThrow(() -> new TraceProcessingException(entryEvent, "Service candidate '" + enteredCandidateName + "' does not exist."));
        
        this.context.currentServiceCandidate(enteredServiceCandidate);
        this.context.currentComponent(targetComponent);
        this.context.currentLocation(targetLocation);
    }

    private void ensureValidLocationTransition(Location sourceLocation, Location targetLocation, ComponentConnection connection, MonitoringEvent event) {
        var locationChanged = !sourceLocation.equals(targetLocation);

        if (connection.isRemote() && !locationChanged) {
            // Remote connections are expected to change the location
            throw new TraceProcessingException(event, "Remote invocation without change of location detected.");
        } else if (!connection.isRemote() && locationChanged) {
            // Location changes with non-remote connections are inadmissible
            throw new TraceProcessingException(event, "Change of location with non-remote invocation detected.");
        }
    }

    @Override
    public Void handleServiceCandidateReturnEvent(ServiceCandidateReturnEvent event) {
        this.context.popCurrentState();

        this.listeners.forEach(listener -> listener.onServiceCandidateReturnEvent(event, this.context));

        return null;
    }

    @Override
    public Void handleTransactionAbortEvent(TransactionAbortEvent event) {
        this.listeners.forEach(listener -> listener.onTransactionAbortEvent(event, this.context));
        return null;
    }

    @Override
    public Void handleTransactionCommitEvent(TransactionCommitEvent event) {
        this.listeners.forEach(listener -> listener.onTransactionCommitEvent(event, this.context));
        return null;
    }

    @Override
    public Void handleTransactionStartEvent(TransactionStartEvent event) {
        this.listeners.forEach(listener -> listener.onTransactionStartEvent(event, this.context));
        return null;
    }

    @Override
    public Void handleUseCaseStartEvent(UseCaseStartEvent event) {
        // Determine the component providing the given use case
        var useCase = new UseCase(event.name());
        
        var component = this.deploymentModel.getComponentForUseCase(useCase)
                .orElseThrow(() -> new TraceProcessingException(event, "Use case '" + useCase + "' is not assigned to a component."));
        

        this.context.currentComponent(component);
        this.context.currentLocation(event.location());

        this.listeners.forEach(listener -> listener.onUseCaseStartEvent(event, this.context));
        return null;
    }

    @Override
    public Void handleUseCaseEndEvent(UseCaseEndEvent event) {
        this.listeners.forEach(listener -> listener.onUseCaseEndEvent(event, this.context));

        this.context.currentServiceCandidate(null);
        this.context.currentComponent(null);
        this.context.currentLocation(null);

        return null;
    }

}
