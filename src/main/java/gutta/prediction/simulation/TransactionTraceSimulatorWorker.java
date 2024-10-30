package gutta.prediction.simulation;

import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionPropagation;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ExplicitTransactionAbortEvent;
import gutta.prediction.event.ImplicitTransactionAbortEvent;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.simulation.Transaction.Demarcation;
import gutta.prediction.simulation.Transaction.Outcome;

import java.util.List;

class TransactionTraceSimulatorWorker extends BasicTraceSimulatorWorker {

    private int syntheticTransactionIdCount = 0;
    
    private Transaction previousTransaction = null;
    
    private Transaction newTransaction = null;

    public TransactionTraceSimulatorWorker(List<TraceSimulationListener> listeners, EventTrace trace, DeploymentModel deploymentModel) {
        super(listeners, trace, deploymentModel);
    }
    
    public TransactionTraceSimulatorWorker(TraceSimulationListener listener, EventTrace trace, DeploymentModel deploymentModel) {
        this(List.of(listener), trace, deploymentModel);
    }

    protected Transaction currentTransaction() {
        return this.context.currentTransaction();
    }

    private String createSyntheticTransactionId() {
        return "synthetic-" + this.syntheticTransactionIdCount++;
    }

    @Override
    protected void handlePossibleTransactionSuspensionOnCandidateInvocation(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ServiceCandidate enteredServiceCandidate, ComponentConnection connection) {
        var currentTransaction = this.context.currentTransaction();
        var newTransaction = this.determineTransactionAfterTransition(invocationEvent, entryEvent, enteredServiceCandidate, connection);

        if (currentTransaction != null && newTransaction != currentTransaction) {
            // If an existing transaction is suspended, notify the listeners (in the old state, and before notifying the listeners of the transition)
            this.listeners.forEach(listener -> listener.onTransactionSuspend(invocationEvent, currentTransaction, this.context));
        }

        this.previousTransaction = currentTransaction;
        this.newTransaction = newTransaction;
    }
    
    @Override
    protected void handlePossibleTransactionCreationOnCandidateEntry(ServiceCandidateEntryEvent entryEvent) {
        this.registerTransactionAndSetAsCurrent(this.newTransaction);
        
        if (newTransaction != null && newTransaction != this.previousTransaction) {
            // If a new transaction is created, notify the listeners (in the new state)
            this.listeners.forEach(listener -> listener.onTransactionStart(entryEvent, newTransaction, this.context));
        }

        this.newTransaction = null;
        this.previousTransaction = null;
    }
    
    @Override
    protected void handlePossibleTransactionCompletionOnCandidateExit(ServiceCandidateExitEvent exitEvent) {
        var transaction = this.currentTransaction();
        var stackEntry = this.context.peek();

        var implicitTopLevelTransactionAvailable = (transaction != null && transaction.demarcation() == Demarcation.IMPLICIT && transaction.isTopLevel());
        if (implicitTopLevelTransactionAvailable && !transaction.equals(stackEntry.transaction())) {
            // If a top-level transaction was implicitly created on entry, we need to complete it
            this.completeTransactionAndNotifyListeners(exitEvent, transaction);
        }
        
        this.previousTransaction = transaction;
    }

    @Override
    protected void handlePossibleTransactionResumeOnCandidateReturn(ServiceCandidateReturnEvent returnEvent) {
        // If we return to a different transaction, notify the listeners that it is resumed
        var restoredTransaction = this.context.currentTransaction();
        if (restoredTransaction != null && restoredTransaction != this.previousTransaction) {
            this.listeners.forEach(listener -> listener.onTransactionResume(returnEvent, restoredTransaction, this.context));
        }
        
        this.previousTransaction = null;
    }

    private void completeTransactionAndNotifyListeners(MonitoringEvent event, Transaction transaction) {
        var outcome = transaction.commit();

        if (outcome == Outcome.COMMITTED) {
            transaction.forEach(tx -> this.notifyListenersOfCommitOf(tx, event));
            transaction.forEach(this::notifyListenersOfCommittedWrites);
        } else {
            transaction.forEach(tx -> this.notifyListenersOfAbortOf(tx, event));
            transaction.forEach(this::notifyListenersOfRevertedWrites);
        }
    }

    protected void notifyListenersOfCommittedWrites(Transaction transaction) {
        // Do nothing by default
    }

    private void notifyListenersOfCommitOf(Transaction transaction, MonitoringEvent event) {
        this.listeners.forEach(listener -> listener.onTransactionCommit(event, transaction, this.context));
    }

    protected void notifyListenersOfRevertedWrites(Transaction transaction) {
        // Do nothing by default
    }

    private void notifyListenersOfAbortOf(Transaction transaction, MonitoringEvent event) {
        this.listeners.forEach(listener -> listener.onTransactionAbort(event, transaction, this.context));
    }
    
    @Override
    protected void handleExplicitStartOfTransaction(TransactionStartEvent event) {
        if (this.context.currentTransaction() != null) {
            throw new TraceProcessingException(event, "A transaction was active at the time of an explicitly demarcated transaction start event.");
        }
        
        var newTransaction = new TopLevelTransaction(event.transactionId(), event, event.location(), Demarcation.EXPLICIT);
        this.listeners.forEach(listener -> listener.onTransactionStart(event, newTransaction, this.context));
        
        this.registerTransactionAndSetAsCurrent(newTransaction);                        
    }
    
    @Override
    protected void handleExplicitCommitOfTransaction(TransactionCommitEvent event) {
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
    }
    
    @Override
    protected void handleExplicitAbortOfTransaction(ExplicitTransactionAbortEvent event) {
        var transaction = this.currentTransaction();
        if (transaction != null) {
            transaction.abort();
                        
            this.listeners.forEach(listener -> listener.onTransactionAbort(event, transaction, this.context));
            transaction.forEach(this::notifyListenersOfRevertedWrites);
        }
    }
    
    @Override
    protected void handleImplicitAbortOfTransaction(ImplicitTransactionAbortEvent event) {
        var transaction = this.currentTransaction();
        if (transaction != null) {
            transaction.registerImplicitAbort(event);
        }
    }
    
    private Transaction determineTransactionAfterTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ServiceCandidate enteredServiceCandidate, ComponentConnection connection) {
        var currentTransaction = this.currentTransaction();
        var propagationType = connection.transactionPropagation();
        
        // We only have a readily usable transaction if it is propagated to the new component 
        var usableTransactionAvailable = (currentTransaction != null && propagationType != TransactionPropagation.NONE);
        var action = this.determineTransactionActionFor(enteredServiceCandidate, usableTransactionAvailable, entryEvent);
        
        switch (action) {
        case CREATE_NEW: 
            // Create a new top-level transaction if required by the action
            var transactionId = (entryEvent.transactionStarted() && entryEvent.transactionId() != null) ? entryEvent.transactionId() : this.createSyntheticTransactionId();
            return new TopLevelTransaction(transactionId, entryEvent, entryEvent.location(), Demarcation.IMPLICIT);
            
        case KEEP:            
            return this.buildAppropriateTransactionFor(currentTransaction, propagationType, entryEvent, entryEvent.location());        
            
        case SUSPEND:
            // If the current transaction is to be suspended, just clear the current transaction
            return null;
            
        default:
            throw new UnsupportedOperationException("Unsupported action '" + action + "'.");
        } 
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
    
    private enum TransactionAction {
        CREATE_NEW,
        KEEP,
        SUSPEND        
    }

}
