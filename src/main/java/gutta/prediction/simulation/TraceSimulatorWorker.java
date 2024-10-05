package gutta.prediction.simulation;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionPropagation;
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
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import gutta.prediction.simulation.Transaction.Demarcation;
import gutta.prediction.simulation.Transaction.Outcome;

import java.util.List;

class TraceSimulatorWorker implements MonitoringEventVisitor<Void> {

    private final List<TraceSimulationListener> listeners;

    private final EventStream events;

    private final DeploymentModel deploymentModel;

    private final TraceSimulationContext context;
        
    private int syntheticLocationIdCount = 0;
    
    private int syntheticTransactionIdCount = 0;

    public TraceSimulatorWorker(TraceSimulationListener listener, EventTrace trace, DeploymentModel deploymentModel) {
        this(List.of(listener), trace, deploymentModel);
    }
    
    public TraceSimulatorWorker(List<TraceSimulationListener> listeners, EventTrace trace, DeploymentModel deploymentModel) {
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
        this.listeners.forEach(TraceSimulationListener::onEndOfProcessing);
    }

    private SyntheticLocation createSyntheticLocation() {
        return new SyntheticLocation(this.syntheticLocationIdCount++);
    }
    
    private String createSyntheticTransactionId() {
        return "synthetic-" + this.syntheticTransactionIdCount++;
    }

    private Component currentComponent() {
        return this.context.currentComponent();
    }
    
    private Transaction currentTransaction() {
        return this.context.currentTransaction();
    }
    
    private Location currentLocation() {
        return this.context.currentLocation();
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
            var sourceComponent = this.currentComponent();
            // Determine the component to return to from the top of the stack
            var targetComponent = this.context.peek().component();
            var connection = this.deploymentModel.getConnection(sourceComponent, targetComponent)
                    .orElseThrow(() -> new TraceProcessingException(event, "No connection from '" + sourceComponent + "' to '" + targetComponent + "'."));

            this.performComponentReturn(event, returnEvent, connection);
        } else {
            throw new IllegalStateException("A service candidate exit event is not followed by a service candidate return event.");
        }

        return null;
    }
    
    private void performComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection) {
        this.listeners.forEach(listener -> listener.onComponentReturn(exitEvent, returnEvent, connection, this.context));
        
        var transaction = this.currentTransaction();
        var stackEntry = this.context.peek();
        
        var implicitTopLevelTransactionAvailable = (transaction != null && transaction.demarcation() == Demarcation.IMPLICIT && transaction.isTopLevel());        
        if (implicitTopLevelTransactionAvailable && !transaction.equals(stackEntry.transaction())) {
            // If a top-level transaction was implicitly created on entry, we need to complete it
            this.completeTransactionAndNotifyListeners(exitEvent, transaction);
        }
        
        // Restore the state from the stack (including location, etc.)
        this.context.popCurrentState();
    }
    
    private void completeTransactionAndNotifyListeners(MonitoringEvent event, Transaction transaction) {
        var outcome = transaction.commit();
        
        if (outcome == Outcome.COMMITTED) {
            transaction.forEach(tx -> this.notifyListenersOfCommitOf(tx, event));
        } else {
            transaction.forEach(tx -> this.notifyListenersOfAbortOf(tx, event));
        }
    }
    
    private void notifyListenersOfCommitOf(Transaction transaction, MonitoringEvent event) {
        this.listeners.forEach(listener -> listener.onTransactionCommit(event, transaction, this.context));
    }
    
    private void notifyListenersOfAbortOf(Transaction transaction, MonitoringEvent event) {
        this.listeners.forEach(listener -> listener.onTransactionAbort(event, transaction, this.context));
    }
            
    @Override
    public Void handleServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event) {
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

        var enteredCandidateName = entryEvent.name();
        var enteredServiceCandidate = this.deploymentModel.resolveServiceCandidateByName(entryEvent.name())
                .orElseThrow(() -> new TraceProcessingException(entryEvent, "Service candidate '" + enteredCandidateName + "' does not exist."));

        // Update the current location and transaction state
        this.updateLocationOnTransition(invocationEvent, entryEvent, enteredServiceCandidate, connection);
        this.updateTransactionOnTransition(entryEvent, enteredServiceCandidate, connection);
    }
     
    private void updateLocationOnTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ServiceCandidate enteredServiceCandidate, ComponentConnection connection) {
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
                targetLocation = this.currentLocation();
            }
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
        
    private void updateTransactionOnTransition(ServiceCandidateEntryEvent entryEvent, ServiceCandidate enteredServiceCandidate, ComponentConnection connection) {
        var currentTransaction = this.currentTransaction();
        var propagationType = connection.transactionPropagation();
        
        // We only have a readily usable transaction if it is propagated to the new component 
        var usableTransactionAvailable = (currentTransaction != null && propagationType != TransactionPropagation.NONE);
        var action = this.determineTransactionActionFor(enteredServiceCandidate, usableTransactionAvailable, entryEvent);
        
        Transaction newTransaction;
        switch (action) {
        case CREATE_NEW: 
            // Create a new top-level transaction if required by the action
            var transactionId = (entryEvent.transactionStarted() && entryEvent.transactionId() != null) ? entryEvent.transactionId() : this.createSyntheticTransactionId();
            newTransaction = new TopLevelTransaction(transactionId, entryEvent, entryEvent.location(), Demarcation.IMPLICIT);
            break;
            
        case KEEP:            
            newTransaction = this.buildAppropriateTransactionFor(currentTransaction, propagationType, entryEvent, entryEvent.location());        
            break;
            
        case SUSPEND:
            // If the current transaction is to be suspended, just clear the current transaction
            newTransaction = null;
            break;
            
        default:
            throw new UnsupportedOperationException("Unsupported action '" + action + "'.");
        }
        
        if (newTransaction != null && newTransaction != currentTransaction) {
            // Notify listeners if a new transaction was started
            this.listeners.forEach(listener -> listener.onTransactionStart(entryEvent, newTransaction, this.context));
        }
                
        this.registerTransactionAndSetAsCurrent(newTransaction);
    }
    
    private TransactionAction determineTransactionActionFor(ServiceCandidate serviceCandidate, boolean transactionAvailable, MonitoringEvent contextEvent) {
        var transactionBehavior = serviceCandidate.transactionBehavior();
        
        switch (transactionBehavior) {
        case MANDATORY:
            if (!transactionAvailable) {
                throw new TraceProcessingException(contextEvent, "No active transaction found for candidate '" + serviceCandidate + "' with behavior " + transactionBehavior + "'.");
            }
            
            return TransactionAction.KEEP; 
            
        case NEVER:
            if (transactionAvailable) {
                throw new TraceProcessingException(contextEvent, "Active transaction found for candidate '" + serviceCandidate + "' with behavior " + transactionBehavior + "'.");
            }
            
            return TransactionAction.KEEP;
            
        case NOT_SUPPORTED:
            return TransactionAction.SUSPEND;
            
        case SUPPORTED:
            return TransactionAction.KEEP;
            
        case REQUIRED:
            return (transactionAvailable) ? TransactionAction.KEEP : TransactionAction.CREATE_NEW;
            
        case REQUIRES_NEW:
            return TransactionAction.CREATE_NEW;
            
        default:
            throw new UnsupportedOperationException("Unsupported transaction behavior '" + transactionBehavior + "'.");
        }            
    }
    
    private Transaction buildAppropriateTransactionFor(Transaction propagatedTransaction, TransactionPropagation propagationType, MonitoringEvent event, Location location) {
        if (propagatedTransaction == null) {
            return null;
        }
        
        switch (propagationType) {
        case IDENTICAL:
            return propagatedTransaction;
            
        case SUBORDINATE:
            return new SubordinateTransaction(this.createSyntheticTransactionId(), event, location, propagatedTransaction);
            
        case NONE:                
            return null;
            
        default:
            throw new IllegalArgumentException("Unsupported propagation type '" + propagationType + "'.");
        }
    }
    
    private void registerTransactionAndSetAsCurrent(Transaction transaction) {
        this.context.currentTransaction(transaction);
    }

    @Override
    public Void handleServiceCandidateReturnEvent(ServiceCandidateReturnEvent event) {
        this.listeners.forEach(listener -> listener.onServiceCandidateReturnEvent(event, this.context));
        return null;
    }

    @Override
    public Void handleImplicitTransactionAbortEvent(ImplicitTransactionAbortEvent event) {
        this.listeners.forEach(listener -> listener.onImplicitTransactionAbortEvent(event, this.context));
        
        var transaction = this.currentTransaction();
        if (transaction != null) {
            transaction.registerImplicitAbort(event);
        }
        
        return null;
    }
    
    @Override
    public Void handleExplicitTransactionAbortEvent(ExplicitTransactionAbortEvent event) {
        this.listeners.forEach(listener -> listener.onExplicitTransactionAbortEvent(event, this.context));
        
        var transaction = this.currentTransaction();
        if (transaction != null) {
            transaction.abort();
            
            this.listeners.forEach(listener -> listener.onTransactionAbort(event, transaction, this.context));
        }
        
        return null;
    }

    @Override
    public Void handleTransactionCommitEvent(TransactionCommitEvent event) {
        this.listeners.forEach(listener -> listener.onTransactionCommitEvent(event, this.context));
        
        var transaction = this.currentTransaction();                
        if (transaction != null) {
            // If a transaction is present, it must be a top-level transaction with explicit demarcation
            if (transaction.isTopLevel() && transaction.demarcation() == Demarcation.EXPLICIT) {
                this.completeTransactionAndNotifyListeners(event, transaction);
            } else {
                throw new IllegalStateException("An invalid transaction '" + transaction + "' was found for explicit commit.");
            }            
            
            this.context.currentTransaction(null);
        }
        
        return null;
    }

    @Override
    public Void handleTransactionStartEvent(TransactionStartEvent event) {
        if (this.context.currentTransaction() != null) {
            throw new TraceProcessingException(event, "A transaction was active at the time of an explicitly demarcated transaction start event.");
        }
        
        var newTransaction = new TopLevelTransaction(event.transactionId(), event, event.location(), Demarcation.EXPLICIT);
        this.listeners.forEach(listener -> listener.onTransactionStart(event, newTransaction, this.context));
        
        this.registerTransactionAndSetAsCurrent(newTransaction);                        
        
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
    
    private enum TransactionAction {
        CREATE_NEW,
        KEEP,
        SUSPEND        
    }

}
