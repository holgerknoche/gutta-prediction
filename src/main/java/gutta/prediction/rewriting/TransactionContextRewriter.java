package gutta.prediction.rewriting;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.ComponentConnections;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionPropagation;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.TransactionStartEvent.Demarcation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionContextRewriter implements TraceRewriter {

    private final List<ServiceCandidate> serviceCandidates;
    
    private final Map<String, Component> useCaseAllocation;
    
    private final Map<ServiceCandidate, Component> candidateAllocation;
    
    private final ComponentConnections connections;
    
    public TransactionContextRewriter(List<ServiceCandidate> serviceCandidates, Map<String, Component> useCaseAllocation, Map<ServiceCandidate, Component> candidateAllocation, ComponentConnections connections) {
        this.serviceCandidates = serviceCandidates;
        this.useCaseAllocation = useCaseAllocation;
        this.candidateAllocation = candidateAllocation;
        this.connections = connections;
    }        
    
    @Override
    public List<MonitoringEvent> rewriteTrace(List<MonitoringEvent> inputTrace) {
        return new TransactionContextRewriterWorker(inputTrace, this.serviceCandidates, this.useCaseAllocation, this.candidateAllocation, this.connections).rewriteTrace();
    }
    
    static class TransactionContextRewriterWorker extends TraceRewriterWorker {                        
        
        private Map<Location, Transaction> openTransactions;
        
        private Transaction currentTransaction;
                
        private PropagatedTransaction propagatedTransaction;
        
        public TransactionContextRewriterWorker(List<MonitoringEvent> events, List<ServiceCandidate> serviceCandidates, Map<String, Component> useCaseAllocation, Map<ServiceCandidate, Component> candidateAllocation,
                ComponentConnections connections) {
            
            super(events, serviceCandidates, useCaseAllocation, candidateAllocation, connections);
        }
                
        @Override
        protected void onStartOfRewrite() {
            this.openTransactions = new HashMap<>();
            this.currentTransaction = null;
            this.propagatedTransaction = null;
        }

        @Override
        protected void onComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ComponentConnection connection) {
            // Save the propagated transaction for later processing at the appropriate event
            this.propagatedTransaction = new PropagatedTransaction(this.currentTransaction, connection.transactionPropagation());
                        
            // Clear the current transaction
            this.currentTransaction = null;
        }
        
        @Override
        protected void onComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection) {
            // Restore the transaction for the location we return to
            this.currentTransaction = this.openTransactions.get(this.currentLocation());
        }
        
        // TODO Affinities (same transaction on revisit)
        
        @Override
        protected void onTransactionStartEvent(TransactionStartEvent event) {
            if (event.demarcation() == Demarcation.EXPLICIT) {
                this.onExplicitTransactionStart(event);
            } else {
                this.onImplicitTransactionStart(event);
            }
        }
        
        private void onExplicitTransactionStart(TransactionStartEvent event) {
            if (this.currentTransaction != null) {
                // When starting a transaction with explicit demarcation, no transaction may be active
                throw new TraceRewriteException(event, "Found an active transaction when an explicit transaction was started.");
            }
            
            var newTransaction = new TopLevelTransaction(event.transactionId());
            this.registerCurrentTransaction(newTransaction);                     
            
            this.addRewrittenEvent(event);
        }                
        
        private void onImplicitTransactionStart(TransactionStartEvent event) {
            var transactionBehavior = this.currentServiceCandidate().transactionBehavior();            
            
            var parentTransaction = (this.propagatedTransaction != null) ? this.propagatedTransaction.transaction() : null;
            Transaction newTransaction;
            
            switch (transactionBehavior) {
            case MANDATORY:
                if (parentTransaction == null) {
                    throw new TraceRewriteException(event, "No transaction available although it is mandatory.");
                }
                
                newTransaction = this.handlePropagatedTransaction(propagatedTransaction, event.transactionId(), true, event);
                break;
                
            case NEVER:
                if (parentTransaction != null) {
                    throw new TraceRewriteException(event, "A transaction was active although it must not be.");
                }
                
                newTransaction = null;
                break;
                
            case NOT_SUPPORTED:
                newTransaction = null;
                break;
            
            case REQUIRED:
                newTransaction = this.handlePropagatedTransaction(propagatedTransaction, event.transactionId(), true, event);
                break;
                
            case REQUIRES_NEW:
                newTransaction = new TopLevelTransaction(event.transactionId());
                break;
                
            case SUPPORTED:
                newTransaction = this.handlePropagatedTransaction(propagatedTransaction, event.transactionId(), false, event);
                break;
                
            default:
                throw new UnsupportedOperationException("Transaction behavior " + transactionBehavior + "' is not supported.");
            }
            
            this.registerCurrentTransaction(newTransaction);
            // TODO Decide whether to include the start event (it might be removed if a remote invocation is moved to a local one). However, we then might need a "removed transaction" marker
            // to remove the commit as well
            this.addRewrittenEvent(event);
        }
        
        private Transaction handlePropagatedTransaction(PropagatedTransaction propagatedTransaction, String transactionId, boolean createIfNecessary, TransactionStartEvent contextEvent) {
            // The propagated transaction may be null if a transaction is created before a service candidate is invoked
            var propagationType = (propagatedTransaction != null) ? propagatedTransaction.propagation() : TransactionPropagation.IDENTICAL;
            var parentTransaction = propagatedTransaction.transaction();
            
            switch (propagationType) {
            case IDENTICAL:
                // For identical propagation, we keep the existing transaction if present
                if (parentTransaction != null) {
                    if (!transactionId.equals(parentTransaction.id())) {                    
                        throw new TraceRewriteException(contextEvent, "Transaction ID '" + transactionId + "' does not match the expected ID '" + parentTransaction.id() + "'.");
                    }
                    
                    return parentTransaction;
                } else {
                    return this.newTopLevelTransaction(transactionId, createIfNecessary);
                }                                
                
            case SUBORDINATE:
                // For subordinate propagation, we create a new transaction subordinate to the propagated transaction
                if (parentTransaction != null) {
                    return new SubordinateTransaction(parentTransaction, transactionId);
                } else {
                    return this.newTopLevelTransaction(transactionId, createIfNecessary);
                }
                
            case NONE:
                // If no propagation is supported, build a new top-level transaction if necessary
                return this.newTopLevelTransaction(transactionId, createIfNecessary); 

            default:
                throw new UnsupportedOperationException("Propagation type '" + propagationType + "' is not supported.");
            }
            
        }
        
        private TopLevelTransaction newTopLevelTransaction(String transactionId, boolean createIfNecessary) {
            return (createIfNecessary) ? new TopLevelTransaction(transactionId) : null;
        }
        
        private void registerCurrentTransaction(Transaction transaction) {
            if (transaction != null) {            
                this.openTransactions.put(this.currentLocation(), transaction);
            }
            
            this.currentTransaction = transaction;
        }
        
        @Override
        protected void onTransactionCommitEvent(TransactionCommitEvent event) {
            if (this.currentTransaction == null) {
                throw new TraceRewriteException(event, "No transaction to commit was available.");
            }
            
            var commitOutcome = this.currentTransaction.commit();            
            
        }
        
        private record PropagatedTransaction(Transaction transaction, TransactionPropagation propagation) {};
        
    }
        
}
