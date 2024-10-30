package gutta.prediction.simulation;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ExplicitTransactionAbortEvent;
import gutta.prediction.event.ImplicitTransactionAbortEvent;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.MonitoringEventVisitor;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.SyntheticLocation;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;

import java.util.List;

class BasicTraceSimulatorWorker extends MonitoringEventVisitor implements TraceSimulatorWorker {

    protected final List<TraceSimulationListener> listeners;    

    protected final DeploymentModel deploymentModel;    
    
    protected final TraceSimulationContext context;
    
    private final EventStream events;
    
    private int syntheticLocationIdCount = 0;        

    public BasicTraceSimulatorWorker(TraceSimulationListener listener, EventTrace trace, DeploymentModel deploymentModel) {
        this(List.of(listener), trace, deploymentModel);
    }
    
    public BasicTraceSimulatorWorker(List<TraceSimulationListener> listeners, EventTrace trace, DeploymentModel deploymentModel) {
        this.listeners = listeners;
        this.events = new EventStream(trace.events());
        this.deploymentModel = deploymentModel;
        this.context = new TraceSimulationContext(deploymentModel, this.events);
    }

    public void processEvents() {
        this.onStartOfProcessing();
        this.processEventsInStream();
        this.onEndOfProcessing();
    }

    private void onStartOfProcessing() {
        // Notify listeners
        this.listeners.forEach(TraceSimulationListener::onStartOfProcessing);
    }

    private void processEventsInStream() {
        this.events.forEach(this::handleMonitoringEvent);
    }

    private void onEndOfProcessing() {
        // Notify listeners
        this.listeners.forEach(TraceSimulationListener::onEndOfProcessing);
    }

    private SyntheticLocation createSyntheticLocation() {
        return new SyntheticLocation(this.syntheticLocationIdCount++);
    }
    
    protected Component currentComponent() {
        return this.context.currentComponent();
    }
        
    protected Location currentLocation() {
        return this.context.currentLocation();
    }
    
    @Override
    protected void handleEntityReadEvent(EntityReadEvent event) {
        this.listeners.forEach(listener -> listener.onEntityReadEvent(event, this.context));
        
        this.updateSimulationOnReadEvent(event);
    }
    
    protected void updateSimulationOnReadEvent(EntityReadEvent event) {
        // Do nothing by default
    }
    
    @Override
    protected void handleEntityWriteEvent(EntityWriteEvent event) {
        this.listeners.forEach(listener -> listener.onEntityWriteEvent(event, this.context));
        
        this.updateSimulationOnWriteEvent(event);
    }
    
    protected void updateSimulationOnWriteEvent(EntityWriteEvent event) {
        // Do nothing by default
    }

    @Override
    protected void handleServiceCandidateEntryEvent(ServiceCandidateEntryEvent event) {
        this.listeners.forEach(listener -> listener.onServiceCandidateEntryEvent(event, this.context));
    }

    @Override
    protected void handleServiceCandidateExitEvent(ServiceCandidateExitEvent event) {
        this.listeners.forEach(listener -> listener.onServiceCandidateExitEvent(event, this.context));

        var nextEvent = this.events.lookahead(1);
        if (nextEvent instanceof ServiceCandidateReturnEvent returnEvent) {
            var sourceComponent = this.currentComponent();
            // Determine the component to return to from the top of the stack
            var targetComponent = this.context.peek().component();
            
            // If the allocation of the current service candidate is synthetic, we need to take this into account for the connection
            var currentCandidate = this.context.currentServiceCandidate();
            var candidateAllocation = this.deploymentModel.getComponentAllocationForServiceCandidate(currentCandidate)
                    .orElseThrow(() -> new TraceProcessingException(event, "Service candidate '" + currentCandidate + "' is not assigned to a component."));
            
            var connection = this.deploymentModel.getConnection(sourceComponent, targetComponent, candidateAllocation.modified())
                    .orElseThrow(() -> new TraceProcessingException(event, "No connection from '" + sourceComponent + "' to '" + targetComponent + "'."));

            this.performComponentReturn(event, returnEvent, connection);
        } else {
            throw new IllegalStateException("A service candidate exit event is not followed by a service candidate return event.");
        }
    }
    
    private void performComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection) {
        this.listeners.forEach(listener -> listener.beforeComponentReturn(exitEvent, returnEvent, connection, this.context));
        
        this.handlePossibleTransactionCompletionOnCandidateExit(exitEvent);
        
        // Restore the state from the stack (including location, etc.)
        this.context.popCurrentState();                

        this.listeners.forEach(listener -> listener.afterComponentReturn(exitEvent, returnEvent, connection, this.context));
        this.handlePossibleTransactionResumeOnCandidateReturn(returnEvent);
    }
    
    protected void handlePossibleTransactionCompletionOnCandidateExit(ServiceCandidateExitEvent exitEvent) {        
        // Do nothing by default
    }
    
    protected void handlePossibleTransactionResumeOnCandidateReturn(ServiceCandidateReturnEvent returnEvent) {
        // Do nothing by default                
    }
            
    @Override
    protected void handleServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event) {
        this.listeners.forEach(listener -> listener.onServiceCandidateInvocationEvent(event, context));

        var nextEvent = this.events.lookahead(1);        
        if (nextEvent instanceof ServiceCandidateEntryEvent entryEvent) {
            var invokedCandidateName = entryEvent.name();
            
            var sourceComponent = this.currentComponent();
            var connection = this.findConnectionForCandidateEntry(invokedCandidateName, sourceComponent, event);
                        
            this.performComponentTransition(event, entryEvent, connection);
        } else {
            throw new IllegalStateException("A service candidate invocation event is not followed by a service candidate entry event.");
        }
    }
    
    private ComponentConnection findConnectionForCandidateEntry(String candidateName, Component sourceComponent, MonitoringEvent event) {
        var invokedCandidate = this.deploymentModel.resolveServiceCandidateByName(candidateName)
                .orElseThrow(() -> new TraceProcessingException(event, "Service candidate '" + candidateName + "' does not exist."));
        var candidateAllocation = this.deploymentModel.getComponentAllocationForServiceCandidate(invokedCandidate)
                .orElseThrow(() -> new TraceProcessingException(event, "Service candidate '" + invokedCandidate + "' is not assigned to a component."));
        
        var targetComponent = candidateAllocation.component();
        return this.deploymentModel.getConnection(sourceComponent, targetComponent, candidateAllocation.modified())
                .orElseThrow(() -> new TraceProcessingException(event, "No connection from '" + sourceComponent + "' to '" + targetComponent + "'."));
    }
    
    private void performComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ComponentConnection connection) {
        var enteredCandidateName = entryEvent.name();
        var enteredServiceCandidate = this.deploymentModel.resolveServiceCandidateByName(entryEvent.name())
                .orElseThrow(() -> new TraceProcessingException(entryEvent, "Service candidate '" + enteredCandidateName + "' does not exist."));

        this.handlePossibleTransactionSuspensionOnCandidateInvocation(invocationEvent, entryEvent, enteredServiceCandidate, connection);

        // Save the current state on the stack before making changes
        this.context.pushCurrentState();
        
        this.listeners.forEach(listener -> listener.beforeComponentTransition(invocationEvent, entryEvent, connection, this.context));
        
        // Update the current location and transaction state
        this.updateLocationOnTransition(invocationEvent, entryEvent, enteredServiceCandidate, connection);
        this.handlePossibleTransactionCreationOnCandidateEntry(entryEvent);
        
        this.listeners.forEach(listener -> listener.afterComponentTransition(invocationEvent, entryEvent, connection, this.context));
    }        
     
    private void updateLocationOnTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ServiceCandidate enteredServiceCandidate, ComponentConnection connection) {
        var targetComponent = connection.target();
        var sourceLocation = this.currentLocation();
        var targetLocation = entryEvent.location();

        if (connection.isRemote()) {
            if (connection.isModified() && !targetLocation.isSynthetic()) {
                // For transitions along modified remote connections, a synthetic target location is required.
                // If the target location is not already synthetic (for instance, due to a previous rewrite), it is created.
                targetLocation = this.createSyntheticLocation();                
            }
        } else {
            // If the connection is not remote, keep the current location
            targetLocation = this.currentLocation();
        }
        
        // Ensure that the transition is valid
        this.ensureValidLocationTransition(sourceLocation, targetLocation, connection, entryEvent);

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
    
    protected void handlePossibleTransactionSuspensionOnCandidateInvocation(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ServiceCandidate enteredServiceCandidate, ComponentConnection connection) {
        // Do nothing by default
    }
    
    protected void handlePossibleTransactionCreationOnCandidateEntry(ServiceCandidateEntryEvent entryEvent) {
        // Do nothing by default
    }

    @Override
    protected void handleServiceCandidateReturnEvent(ServiceCandidateReturnEvent event) {
        this.listeners.forEach(listener -> listener.onServiceCandidateReturnEvent(event, this.context));
    }

    @Override
    protected void handleImplicitTransactionAbortEvent(ImplicitTransactionAbortEvent event) {
        this.listeners.forEach(listener -> listener.onImplicitTransactionAbortEvent(event, this.context));
        
        this.handleImplicitAbortOfTransaction(event);
    }
    
    protected void handleImplicitAbortOfTransaction(ImplicitTransactionAbortEvent event) {
        // Do nothing by default
    }
    
    @Override
    protected void handleExplicitTransactionAbortEvent(ExplicitTransactionAbortEvent event) {
        this.listeners.forEach(listener -> listener.onExplicitTransactionAbortEvent(event, this.context));
        
        this.handleExplicitAbortOfTransaction(event);
    }

    protected void handleExplicitAbortOfTransaction(ExplicitTransactionAbortEvent event) {
        // Do nothing by default
    }
    
    @Override
    protected void handleTransactionCommitEvent(TransactionCommitEvent event) {
        this.listeners.forEach(listener -> listener.onTransactionCommitEvent(event, this.context));
        
        this.handleExplicitCommitOfTransaction(event);
    }
    
    protected void handleExplicitCommitOfTransaction(TransactionCommitEvent event) {
        // Do nothing by default
    }

    @Override
    protected void handleTransactionStartEvent(TransactionStartEvent event) {
        this.handleExplicitStartOfTransaction(event);
        
        this.listeners.forEach(listener -> listener.onTransactionStartEvent(event, this.context));
    }
    
    protected void handleExplicitStartOfTransaction(TransactionStartEvent event) {
        // Do nothing by default
    }

    @Override
    protected void handleUseCaseStartEvent(UseCaseStartEvent event) {
        // Determine the component providing the given use case
        var useCase = new UseCase(event.name());
        
        var componentAllocation = this.deploymentModel.getComponentAllocationForUseCase(useCase)
                .orElseThrow(() -> new TraceProcessingException(event, "Use case '" + useCase + "' is not assigned to a component."));
        

        this.context.currentComponent(componentAllocation.component());
        this.context.currentLocation(event.location());

        this.listeners.forEach(listener -> listener.onUseCaseStartEvent(event, this.context));
    }

    @Override
    protected void handleUseCaseEndEvent(UseCaseEndEvent event) {
        this.listeners.forEach(listener -> listener.onUseCaseEndEvent(event, this.context));

        this.context.currentServiceCandidate(null);
        this.context.currentComponent(null);
        this.context.currentLocation(null);
    }    

}
