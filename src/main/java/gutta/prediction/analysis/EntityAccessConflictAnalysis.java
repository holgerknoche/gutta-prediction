package gutta.prediction.analysis;

import gutta.prediction.common.AbstractMonitoringEventProcessor;
import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnections;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionAbortEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EntityAccessConflictAnalysis {
    
    public void performAnalysis(List<MonitoringEvent> events, Collection<ServiceCandidate> serviceCandidates, Map<String, Component> useCaseAllocation, Map<String, Component> methodAllocation,
            ComponentConnections connections) {
        new EntityAccessConflictAnalysisWorker(events, serviceCandidates, useCaseAllocation, methodAllocation, connections).performAnalysis();
    }
    
    private static class EntityAccessConflictAnalysisWorker extends AbstractMonitoringEventProcessor {
        
        private final Map<String, ServiceCandidate> serviceCandidateLookup;
                
        private Deque<Transaction> stack;
        
        private Map<Location, Transaction> boundTransactions;
        
        public EntityAccessConflictAnalysisWorker(List<MonitoringEvent> events, Collection<ServiceCandidate> serviceCandidates, Map<String, Component> useCaseAllocation, Map<String, Component> methodAllocation,
                ComponentConnections connections) {
            
            super(events, useCaseAllocation, methodAllocation, connections);
            this.serviceCandidateLookup = serviceCandidates.stream().collect(Collectors.toMap(ServiceCandidate::name, Function.identity()));
        }
        
        public void performAnalysis() {
            this.stack = new ArrayDeque<>();
            this.boundTransactions = new HashMap<>();
            
            this.processEvents();            
        }
        
        @Override
        public Void handleUseCaseStartEvent(UseCaseStartEvent event) {            
            // Not important for this type of analysis
            return null;
        }
        
        @Override
        public Void handleUseCaseEndEvent(UseCaseEndEvent event) {
         // Not important for this type of analysis
            return null;
        }
        
        private void assertNoBoundTransactionAt(Location location) {
            if (this.boundTransactions.containsKey(location)) {
                throw new IllegalStateException("There is already a transaction bound at location '" + location + "'.");
            }
        }
        
        private Optional<Transaction> getBoundTransactionAt(Location location) {
            return Optional.ofNullable(this.boundTransactions.get(location));            
        }

        @Override
        public Void handleServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event) {
            // TODO Auto-generated method stub
            return super.handleServiceCandidateInvocationEvent(event);
        }
        
        @Override
        public Void handleServiceCandidateEntryEvent(ServiceCandidateEntryEvent event) {
            // TODO Auto-generated method stub
            return super.handleServiceCandidateEntryEvent(event);
        }
        
        @Override
        public Void handleServiceCandidateExitEvent(ServiceCandidateExitEvent event) {
            // TODO Auto-generated method stub
            return super.handleServiceCandidateExitEvent(event);
        }
        
        @Override
        public Void handleServiceCandidateReturnEvent(ServiceCandidateReturnEvent event) {
            // TODO Auto-generated method stub
            return super.handleServiceCandidateReturnEvent(event);
        }
        
        @Override
        public Void handleTransactionStartEvent(TransactionStartEvent event) {
            var currentLocation = event.location();
            this.assertNoBoundTransactionAt(currentLocation);
            
            var transaction = new Transaction(event.transactionId());
            this.boundTransactions.put(currentLocation, transaction);
            
            return null;
        }
        
        @Override
        public Void handleTransactionCommitEvent(TransactionCommitEvent event) {
            var currentLocation = event.location();
            var boundTransaction = this.getBoundTransactionAt(currentLocation);
            
            System.out.println(event);
            
            // TODO Auto-generated method stub
            return super.handleTransactionCommitEvent(event);
        }
        
        @Override
        public Void handleTransactionAbortEvent(TransactionAbortEvent event) {
            System.out.println(event);
            
            // TODO Auto-generated method stub
            // TODO Register pending writes that were rolled back
            return super.handleTransactionAbortEvent(event);
        }
        
        @Override
        public Void handleEntityReadEvent(EntityReadEvent event) {
            var optionalTransaction = this.getBoundTransactionAt(event.location());
            
            if (optionalTransaction.isPresent()) {            
                var entity = new Entity(event.entityType(), event.entityIdentifier());
                
                var transaction = optionalTransaction.get();
                if (transaction.hasConflictForEntity(entity)) {
                    // TODO Report stale-read / deadlock conflict
                    System.out.println("Stale-Read conflict for entity " + entity);
                }
            }
            
            return null;
        }
        
        @Override
        public Void handleEntityWriteEvent(EntityWriteEvent event) {
            var optionalTransaction = this.getBoundTransactionAt(event.location());
            
            if (optionalTransaction.isPresent()) {            
                var entity = new Entity(event.entityType(), event.entityIdentifier());
                
                var transaction = optionalTransaction.get();
                if (transaction.hasConflictForEntity(entity)) {
                    // TODO Report write-write conflict
                    System.out.println("Write conflict for entity " + entity);
                }
                
                transaction.registerWriteForEntity(entity);
            }
            
            return null;
        }
        
    }
        
    // TODO Transaction attributes (mandatory, required, ...)
    // TODO Discover conflicts between suspended transaction and active transaction
    
    private static class Transaction {
        
        private final Transaction parent;
        
        private final String id;
        
        private final Set<Entity> pendingWrites = new HashSet<>();
                
        public Transaction(String id) {
            this(id, null);
        }
        
        public Transaction(String id, Transaction parent) {
            this.id = id;
            this.parent = parent;
        }
        
        public boolean hasConflictForEntity(Entity entity) {
            // TODO
            return false;
        }
        
        public void registerWriteForEntity(Entity entity) {
            // TODO Register write with parent
            this.pendingWrites.add(entity);
        }
        
    }
    
    private record Entity(String entityType, String entityId) {}

}
