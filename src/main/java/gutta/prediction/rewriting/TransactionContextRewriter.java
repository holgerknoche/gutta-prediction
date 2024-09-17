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
        
        private int syntheticTransactionIdCount;
        
        private Transaction currentTransaction;
        
        private PropagatedTransaction propagatedTransaction;
        
        public TransactionContextRewriterWorker(List<MonitoringEvent> events, List<ServiceCandidate> serviceCandidates, Map<String, Component> useCaseAllocation, Map<ServiceCandidate, Component> candidateAllocation,
                ComponentConnections connections) {
            
            super(events, serviceCandidates, useCaseAllocation, candidateAllocation, connections);
        }
                
        @Override
        protected void onStartOfRewrite() {
            this.transactionAtLocation = new HashMap<>();
            this.removedTransactionIds = new HashSet<>();
            this.syntheticTransactionIdCount = 0;
            this.currentTransaction = null;
            this.propagatedTransaction = null;
        }

        @Override
        protected void onComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ComponentConnection connection) {
            switch (connection.transactionPropagation()) {
            case IDENTICAL:
                // For identical propagation, we simply keep the current transaction
                this.propagatedTransaction = null;
                break;
                
            case SUBORDINATE:
                // For subordinate propagation, a new local transaction must be created
                this.propagatedTransaction = new PropagatedTransaction(this.currentTransaction, connection.transactionPropagation());
                this.currentTransaction = null;
                break;
                
            case NONE:
                // If no propagation occurs, remove the current and propagated transactions
                this.propagatedTransaction = null;
                this.currentTransaction = null;
                break;

            default:
                throw new UnsupportedOperationException("Unsupported propagation mode '" + connection.transactionPropagation() + "'.");
            }
        }
        
        private Transaction buildLocalTransactionFor(PropagatedTransaction propagatedTransaction, Location location) {
            var propagationType = propagatedTransaction.propagationType();
            
            switch (propagationType) {
            case IDENTICAL:
                return propagatedTransaction.transaction();
                
            case SUBORDINATE:
                return new SubordinateTransaction(this.createSyntheticTransactionId(), location, true, propagatedTransaction.transaction());
                
            case NONE:                
                return null;
                
            default:
                throw new IllegalArgumentException("Unsupported propagation type '" + propagationType + "'.");
            }
        }
                
        @Override
        protected void onComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection) {
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
            
            var transactionStartEventPresent = (this.lookahead(1) instanceof TransactionStartEvent);
            var transactionAvailable = (this.currentTransaction != null);
            var transactionStartEventRequired = this.isTransactionStartRequiredOn(serviceCandidate, transactionAvailable);
              
            // We must only change something when the actual state does not fit the expectation
            if (transactionStartEventPresent && !transactionStartEventRequired) {
                // TODO Transaction event present, but not required => delete transaction
            } else if (!transactionStartEventPresent && transactionStartEventRequired) {
                // Transaction event not present, but required => create a synthetic transaction and insert synthetic event

                // Create a synthetic transaction
                Transaction syntheticTransaction;
                if (this.propagatedTransaction != null) {
                    syntheticTransaction = this.buildLocalTransactionFor(propagatedTransaction, event.location());
                    this.propagatedTransaction = null;
                } else {
                    syntheticTransaction = new TopLevelTransaction(this.createSyntheticTransactionId(), this.currentLocation(), true);
                }
                this.registerTransactionAndSetAsCurrent(syntheticTransaction);
                
                // Insert a synthetic transaction start event
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
            var previousEvent = this.lookback(1);
            var transactionEndEventExists = (previousEvent instanceof TransactionCommitEvent || previousEvent instanceof TransactionAbortEvent);
            var transactionEndRequired = (this.currentTransaction != null);
            
            // We only need to make changes if an event is missing, surplus events have been removed earlier             
            if (!transactionEndEventExists && transactionEndRequired && this.currentTransaction.isSynthetic()) {
                // TODO Maybe better use a stack to associate synthetic transactions with commits
                // If a synthetic transaction was created (i.e., a synthetic start event was inserted), we must also insert a synthetic end event
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
            
            var newTransaction = new TopLevelTransaction(event.transactionId(), event.location(), false);
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
        
        private final boolean synthetic;
        
        private final Set<SubordinateTransaction> subordinates;
        
        protected Transaction(String id, Location location, boolean synthetic) {
            this.id = id;
            this.location = location;
            this.synthetic = synthetic;
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
        
        public boolean isSynthetic() {
            return this.synthetic;
        }
        
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
        
        public TopLevelTransaction(String id, Location location, boolean synthetic) {
            super(id, location, synthetic);
        }                
        
        @Override
        public boolean isSubordinate() {
            return false;
        }
                
    }
    
    private static class SubordinateTransaction extends Transaction {
                
        public SubordinateTransaction(String id, Location location, boolean synthetic, Transaction parent) {
            super(id, location, synthetic);
            
            parent.registerSubordinate(this);
        }
        
        @Override
        public boolean isSubordinate() {
            return true;
        }
        
    }
    
    private record PropagatedTransaction(Transaction transaction, TransactionPropagation propagationType) {}
            
}
