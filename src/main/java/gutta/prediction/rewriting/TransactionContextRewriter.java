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
import gutta.prediction.event.TransactionAbortEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.TransactionStartEvent.Demarcation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

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

        private Map<Location, Transaction> transactionAtLocation;
        
        private Set<String> removedTransactionIds;
        
        private Deque<PropagatedTransaction> transactionPropagationStack;
        
        private int syntheticTransactionIdCount;
        
        private Transaction currentTransaction;
                
        public TransactionContextRewriterWorker(List<MonitoringEvent> events, List<ServiceCandidate> serviceCandidates, Map<String, Component> useCaseAllocation, Map<ServiceCandidate, Component> candidateAllocation,
                ComponentConnections connections) {
            
            super(events, serviceCandidates, useCaseAllocation, candidateAllocation, connections);
        }
                
        @Override
        protected void onStartOfRewrite() {
            this.transactionAtLocation = new HashMap<>();
            this.removedTransactionIds = new HashSet<>();
            this.transactionPropagationStack = new ArrayDeque<>();
            this.syntheticTransactionIdCount = 0;
            this.currentTransaction = null;
        }

        @Override
        protected void onComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ComponentConnection connection) {
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
        protected void onComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection) {
            var propagatedTransaction = this.transactionPropagationStack.pop();
            
            // Restore the current transaction
            this.currentTransaction = propagatedTransaction.transaction();
        }
        
        // TODO Affinities (potentially same transaction on revisit)
        
        private String createSyntheticTransactionId() {
            return "synthetic-" + this.syntheticTransactionIdCount++;
        }
        
        @Override
        protected void onServiceCandidateEntryEvent(ServiceCandidateEntryEvent event) {
            // In any case, the entry event is copied / adjusted
            this.adjustLocationAndAdd(event);
            
            // Then, we have to determine if we need to add / remove a transaction start event
            var serviceCandidate = this.resolveCandidate(event.name());
            var propagatedTransaction = this.transactionPropagationStack.peek();
            
            var transactionStartEventPresent = (this.lookahead(1) instanceof TransactionStartEvent);
            // We only have a readily usable transaction if it is propagated identically, otherwise we may need to create a local branch first 
            var usableTransactionAvailable = (propagatedTransaction.transaction() != null && propagatedTransaction.propagationType() == TransactionPropagation.IDENTICAL);
            var transactionStartEventRequired = this.isTransactionStartRequiredOn(serviceCandidate, usableTransactionAvailable);
              
            // We must only change something when the actual state does not fit the expectation
            if (transactionStartEventPresent && !transactionStartEventRequired) {
                // If a transaction start event is present, but is no longer required, mark the respective transaction ID for removal
                var transactionStartEvent = (TransactionStartEvent) this.lookahead(1);
                this.removedTransactionIds.add(transactionStartEvent.transactionId());
            } else if (!transactionStartEventPresent && transactionStartEventRequired) {
                // If a transaction start event is missing although it is required, insert a synthetic transaction
                Transaction syntheticTransaction;
                if (propagatedTransaction.transaction() != null) {
                    syntheticTransaction = this.buildLocalTransactionFor(propagatedTransaction, event.location());
                } else {
                    syntheticTransaction = new TopLevelTransaction(this.createSyntheticTransactionId(), this.currentLocation());
                }
                this.registerTransactionAndSetAsCurrent(syntheticTransaction);                
                
                // Insert a synthetic transaction start event for the transaction
                var syntheticStartEvent = new TransactionStartEvent(event.traceId(), event.timestamp(), this.currentLocation(), syntheticTransaction.id(), Demarcation.IMPLICIT);                
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
        protected void onServiceCandidateExitEvent(ServiceCandidateExitEvent event) {
            var propagatedTransaction = this.transactionPropagationStack.peek();
            
            var previousEvent = this.lookback(1);
            var transactionEndEventExists = (previousEvent instanceof TransactionCommitEvent || previousEvent instanceof TransactionAbortEvent);
            // We have to insert a transaction end event if the transaction changed on the transition, i.e., if it is different from the propagated transaction  
            var transactionEndRequired = (this.currentTransaction != null && !this.currentTransaction.equals(propagatedTransaction.transaction()));
            
            // We only need to make changes if an event is missing, superfluous commit events are removed as they are encountered             
            if (!transactionEndEventExists && transactionEndRequired) {
                var syntheticCommitEvent = new TransactionCommitEvent(event.traceId(), event.timestamp(), this.currentLocation(), this.currentTransaction.id());
                this.addRewrittenEvent(syntheticCommitEvent);
            }
            
            this.adjustLocationAndAdd(event);
        }
        
        @Override
        protected void onTransactionStartEvent(TransactionStartEvent event) {
            // TODO Question: Do we keep this event or do we remove it?
            
            if (event.demarcation() == Demarcation.EXPLICIT) {
                this.startExplicitlyDemarcatedTransaction(event);     
            } else {
                this.startImplicitlyDemarcatedTransaction(event);
            }                        
        }
        
        private void startExplicitlyDemarcatedTransaction(TransactionStartEvent event) {
            if (this.currentTransaction != null) {
                throw new TraceRewriteException(event, "A transaction was active at the time of an explicitly demarcated transaction start event.");
            }
            
            var newTransaction = new TopLevelTransaction(event.transactionId(), event.location());
            this.registerTransactionAndSetAsCurrent(newTransaction);
            
            // Explicitly demarcated transaction start events are always kept
            this.addRewrittenEvent(event);
        }
        
        private void registerTransactionAndSetAsCurrent(Transaction transaction) {
            this.transactionAtLocation.put(this.currentLocation(), transaction);
            this.currentTransaction = transaction;
        }
                
        private void startImplicitlyDemarcatedTransaction(TransactionStartEvent event) {
            // TODO
        }
        
        @Override
        protected void onTransactionCommitEvent(TransactionCommitEvent event) {
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
