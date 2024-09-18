package gutta.prediction.rewriting;

import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionPropagation;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionAbortEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.TransactionStartEvent.Demarcation;
import gutta.prediction.stream.EventProcessingContext;
import gutta.prediction.stream.TraceProcessingException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class TransactionContextRewriter implements TraceRewriter {

    private final DeploymentModel deploymentModel;
        
    public TransactionContextRewriter(DeploymentModel deploymentModel) {
        this.deploymentModel = deploymentModel;
    }        
    
    @Override
    public List<MonitoringEvent> rewriteTrace(List<MonitoringEvent> inputTrace) {
        return new TransactionContextRewriterWorker().rewriteTrace(inputTrace, this.deploymentModel);
    }
    
    private static class TransactionContextRewriterWorker extends TraceRewriterWorker {                        

        private Map<Location, Transaction> transactionAtLocation;
        
        private Set<String> removedTransactionIds;
        
        private Deque<PropagatedTransaction> transactionPropagationStack;
        
        private int syntheticTransactionIdCount;
        
        private Transaction currentTransaction;
                                
        @Override
        protected void onStartOfRewrite() {
            this.transactionAtLocation = new HashMap<>();
            this.removedTransactionIds = new HashSet<>();
            this.transactionPropagationStack = new ArrayDeque<>();
            this.syntheticTransactionIdCount = 0;
            this.currentTransaction = null;
        }

        @Override
        public void onComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ComponentConnection connection, EventProcessingContext context) {
            var propagatedTransaction = new PropagatedTransaction(this.currentTransaction, connection.transactionPropagation());
            this.transactionPropagationStack.push(propagatedTransaction);
        }
        
        private Transaction buildLocalTransactionFor(PropagatedTransaction propagatedTransaction, Location location) {
            var propagationType = propagatedTransaction.propagationType();
            
            switch (propagationType) {
            case IDENTICAL:
                return propagatedTransaction.transaction();
                
            case SUBORDINATE:
                return new SubordinateTransaction(this.createSyntheticTransactionId(), location, propagatedTransaction.transaction());
                
            case NONE:                
                return null;
                
            default:
                throw new IllegalArgumentException("Unsupported propagation type '" + propagationType + "'.");
            }
        }
                
        @Override
        public void onComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection, EventProcessingContext context) {
            var propagatedTransaction = this.transactionPropagationStack.pop();
            
            // Restore the current transaction
            this.currentTransaction = propagatedTransaction.transaction();
        }
        
        // TODO Affinities (potentially same transaction on revisit)
        
        private String createSyntheticTransactionId() {
            return "synthetic-" + this.syntheticTransactionIdCount++;
        }
        
        @Override
        public void onServiceCandidateEntryEvent(ServiceCandidateEntryEvent event, EventProcessingContext context) {
            // In any case, the entry event is copied / adjusted
            this.adjustLocationAndAdd(event, context);
            
            // Then, we have to determine if we need to add / remove a transaction start event
            var serviceCandidateName = event.name();
            var serviceCandidate = context.deploymentModel().resolveServiceCandidateByName(serviceCandidateName).orElseThrow(() -> new TraceProcessingException(event, "Service candidate '" + serviceCandidateName + "' does not exist."));
            var propagatedTransaction = this.transactionPropagationStack.peek();
            
            var transactionStartEventPresent = (context.lookahead(1) instanceof TransactionStartEvent);
            // We only have a readily usable transaction if it is propagated identically, otherwise we may need to create a local branch first 
            var usableTransactionAvailable = (propagatedTransaction.transaction() != null && propagatedTransaction.propagationType() == TransactionPropagation.IDENTICAL);
            var transactionStartEventRequired = this.isTransactionStartRequiredOn(serviceCandidate, usableTransactionAvailable);
              
            // We must only change something when the actual state does not fit the expectation
            if (transactionStartEventPresent && !transactionStartEventRequired) {
                // If a transaction start event is present, but is no longer required, mark the respective transaction ID for removal
                var transactionStartEvent = (TransactionStartEvent) context.lookahead(1);
                this.removedTransactionIds.add(transactionStartEvent.transactionId());
            } else if (!transactionStartEventPresent && transactionStartEventRequired) {
                // If a transaction start event is missing although it is required, insert a synthetic transaction
                Transaction syntheticTransaction;
                if (propagatedTransaction.transaction() != null) {
                    syntheticTransaction = this.buildLocalTransactionFor(propagatedTransaction, event.location());
                } else {
                    syntheticTransaction = new TopLevelTransaction(this.createSyntheticTransactionId(), context.currentLocation());
                }
                this.registerTransactionAndSetAsCurrent(syntheticTransaction, context);                
                
                // Insert a synthetic transaction start event for the transaction
                var syntheticStartEvent = new TransactionStartEvent(event.traceId(), event.timestamp(), context.currentLocation(), syntheticTransaction.id(), Demarcation.IMPLICIT);                
                this.addRewrittenEvent(syntheticStartEvent);                
            }
        }
        
        private boolean isTransactionStartRequiredOn(ServiceCandidate serviceCandidate, boolean transactionAvailable) {
            var transactionBehavior = serviceCandidate.transactionBehavior();
            
            switch (transactionBehavior) {
            case MANDATORY:
            case NEVER:
            case NOT_SUPPORTED:
            case SUPPORTED:
                // For these behaviors, no transaction is ever created
                return false;
                
            case REQUIRED:
                // A transaction is only created if no transaction is available
                return !transactionAvailable;
                
            case REQUIRES_NEW:
                // This behavior always creates a new transaction
                return true;
                
            default:
                throw new UnsupportedOperationException("Unsupported transaction behavior '" + transactionBehavior + "'.");
            }            
        }
        
        @Override
        public void onServiceCandidateExitEvent(ServiceCandidateExitEvent event, EventProcessingContext context) {
            var propagatedTransaction = this.transactionPropagationStack.peek();
            
            var previousEvent = context.lookback(1);
            var transactionEndEventExists = (previousEvent instanceof TransactionCommitEvent || previousEvent instanceof TransactionAbortEvent);
            // We have to insert a transaction end event if the transaction changed on the transition, i.e., if it is different from the propagated transaction  
            var transactionEndRequired = (this.currentTransaction != null && !this.currentTransaction.equals(propagatedTransaction.transaction()));
            
            // We only need to make changes if an event is missing, superfluous commit events are removed as they are encountered             
            if (!transactionEndEventExists && transactionEndRequired) {
                var syntheticCommitEvent = new TransactionCommitEvent(event.traceId(), event.timestamp(), context.currentLocation(), this.currentTransaction.id());
                this.addRewrittenEvent(syntheticCommitEvent);
            }
            
            this.adjustLocationAndAdd(event, context);
        }
        
        @Override
        public void onTransactionStartEvent(TransactionStartEvent event, EventProcessingContext context) {
            // TODO Question: Do we keep this event or do we remove it?
            
            if (event.demarcation() == Demarcation.EXPLICIT) {
                this.startExplicitlyDemarcatedTransaction(event, context);     
            } else {
                this.startImplicitlyDemarcatedTransaction(event);
            }                        
        }
        
        private void startExplicitlyDemarcatedTransaction(TransactionStartEvent event, EventProcessingContext context) {
            if (this.currentTransaction != null) {
                throw new TraceRewriteException(event, "A transaction was active at the time of an explicitly demarcated transaction start event.");
            }
            
            var newTransaction = new TopLevelTransaction(event.transactionId(), event.location());
            this.registerTransactionAndSetAsCurrent(newTransaction, context);
            
            // Explicitly demarcated transaction start events are always kept
            this.addRewrittenEvent(event);
        }
        
        private void registerTransactionAndSetAsCurrent(Transaction transaction, EventProcessingContext context) {
            this.transactionAtLocation.put(context.currentLocation(), transaction);
            this.currentTransaction = transaction;
        }
                
        private void startImplicitlyDemarcatedTransaction(TransactionStartEvent event) {
            // TODO
        }
        
        @Override
        public void onTransactionCommitEvent(TransactionCommitEvent event, EventProcessingContext context) {
            if (this.currentTransaction != null) {
                // If a transaction is active, remove it and all its subordinates and keep the commit event
                var transactionsToRemove = this.currentTransaction.getThisAndAllSubordinates();
                transactionsToRemove.forEach(transaction -> this.transactionAtLocation.remove(transaction.location()));
                this.currentTransaction = null;
                
                this.addRewrittenEvent(event);
            }            
        }                
                
    }
    
    private abstract static class Transaction {
        
        private final String id;
        
        private final Location location;
                
        private final Set<SubordinateTransaction> subordinates;
        
        protected Transaction(String id, Location location) {
            this.id = id;
            this.location = location;
            this.subordinates = new HashSet<>();
        }
        
        public String id() {
            return this.id;
        }
        
        public Location location() {
            return this.location;
        }
        
        protected void registerSubordinate(SubordinateTransaction subordinate) {
            this.subordinates.add(subordinate);
        }
        
        public abstract boolean isSubordinate();
                
        public Set<Transaction> getThisAndAllSubordinates() {
            var allSubordinates = new HashSet<Transaction>();
            
            this.collectSubordinates(allSubordinates::add);
            
            return allSubordinates;
        }
        
        private void collectSubordinates(Consumer<Transaction> collector) {
            collector.accept(this);
            
            for (Transaction subordinate : this.subordinates) {
                subordinate.collectSubordinates(collector);
            }
        }
        
    }
    
    private static class TopLevelTransaction extends Transaction {
        
        public TopLevelTransaction(String id, Location location) {
            super(id, location);
        }                
        
        @Override
        public boolean isSubordinate() {
            return false;
        }
                
    }
    
    private static class SubordinateTransaction extends Transaction {
                
        public SubordinateTransaction(String id, Location location, Transaction parent) {
            super(id, location);
            
            parent.registerSubordinate(this);
        }
        
        @Override
        public boolean isSubordinate() {
            return true;
        }
        
    }
        
    private record PropagatedTransaction(Transaction transaction, TransactionPropagation propagationType) {}
            
}
