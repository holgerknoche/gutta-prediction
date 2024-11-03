package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.Entity;
import gutta.prediction.domain.ReadWriteConflictBehavior;
import gutta.prediction.event.EntityAccessEvent;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.simulation.TraceProcessingException;
import gutta.prediction.simulation.TraceSimulationContext;
import gutta.prediction.simulation.TraceSimulationListener;
import gutta.prediction.simulation.TraceSimulationMode;
import gutta.prediction.simulation.Transaction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static gutta.prediction.simulation.TraceSimulator.runSimulationOf;

class ConsistencyIssuesAnalyzer implements TraceSimulationListener {
    
    private final boolean checkForCrossComponentAccesses;
    
    private final boolean checkForInterleavingAccesses;
        
    private final Set<ConsistencyIssue<?>> foundIssues = new HashSet<>();
    
    private final Set<EntityWriteEvent> committedWrites = new HashSet<>();
    
    private final Set<EntityWriteEvent> revertedWrites = new HashSet<>();
    
    private final Set<Transaction> suspendedTransactions = new HashSet<>();
    
    private final Map<Transaction, TransactionData> transactionContextData = new HashMap<>();
    
    private DeploymentModel deploymentModel;
    
    ConsistencyIssuesAnalyzer() {
        this(CheckCrossComponentAccesses.YES, CheckInterleavingAccesses.YES);
    }
    
    ConsistencyIssuesAnalyzer(CheckCrossComponentAccesses checkCrossComponentAccesses, CheckInterleavingAccesses checkInterleavingAccesses) {
        this.checkForCrossComponentAccesses = (checkCrossComponentAccesses == CheckCrossComponentAccesses.YES);
        this.checkForInterleavingAccesses = (checkInterleavingAccesses == CheckInterleavingAccesses.YES);
    }       
    
    public ConsistencyAnalyzerResult analyzeTrace(EventTrace trace, DeploymentModel deploymentModel) {
        this.deploymentModel = deploymentModel;
        
        runSimulationOf(trace, deploymentModel, TraceSimulationMode.WITH_ENTITY_ACCESS, this);
        
        return new ConsistencyAnalyzerResult(this.foundIssues, this.committedWrites, this.revertedWrites);
    }
     
    @Override
    public void onEntityReadEvent(EntityReadEvent event, TraceSimulationContext context) {
        if (this.checkForCrossComponentAccesses) {
            this.assertValidComponent(event, context);
        }
    }
    
    @Override
    public void onEntityWriteEvent(EntityWriteEvent event, TraceSimulationContext context) {
        if (this.checkForCrossComponentAccesses) {
            this.assertValidComponent(event, context);
        }
        
        if (this.checkForInterleavingAccesses) {
            this.checkForInterleavedChange(event, context);
            this.updateInterleavingForSuspendedTransactions();
        }
    }
    
    private void assertValidComponent(EntityAccessEvent event, TraceSimulationContext context) {
        var entityType = event.entity().type();
        var componentAllocation = this.deploymentModel.getComponentAllocationForEntityType(entityType)
                .orElseThrow(() -> new TraceProcessingException(event, "Entity type " + entityType + " is not assigned to a component."));
        
        if (!componentAllocation.component().equals(context.currentComponent())) {
            var issue = new CrossComponentAccessIssue(event.entity(), event);
            this.foundIssues.add(issue);
        }
    }
    
    private void checkForInterleavedChange(EntityWriteEvent event, TraceSimulationContext context) {
        var currentTransaction = context.currentTransaction();
        if (currentTransaction == null) {
            // If no transaction is active, there can be no interleaved change
            return;
        }
        
        var dataForTransaction = this.transactionContextData.get(currentTransaction);
        
        if (dataForTransaction != null) {
            // If there is data for this transaction, check for an interleaved write for the respective entity
            var changedEntity = event.entity();            
            this.checkForInterleavedChange(event, changedEntity, dataForTransaction);
        } else {
            // If there was no data for this transaction yet, create an entry
            dataForTransaction = new TransactionData();
            this.transactionContextData.put(currentTransaction, dataForTransaction);
        }
        
        // In any case, register the current write
        dataForTransaction.registerWrite(event);
    }
    
    private void checkForInterleavedChange(EntityWriteEvent event, Entity entity, TransactionData transactionData) {
        var change = transactionData.existingChangeForEntity(entity);
        if (isInterleavedChange(change)) {
            // Same entity was changed before, so raise an "interleaved change" event
            var issue = new InterleavedWriteIssue(entity, event);
            this.foundIssues.add(issue);
        } 
        
        if (entity.hasRoot()) {
            var rootType = entity.type().rootType();            
            if (rootType == null) {
                throw new TraceProcessingException(event, "Entity type '" + entity.type() + "' does not have a root type.");
            }
            
            var rootEntity = new Entity(rootType, entity.rootId());
            var rootChange = transactionData.existingChangeForEntity(rootEntity);
            if (isInterleavedChange(rootChange)) {
                // Root entity or another subordinate was changed before, so raise an "interleaved change" event
                var issue = new InterleavedWriteIssue(entity, event);
                this.foundIssues.add(issue);
            }
        }
    }
    
    private static boolean isInterleavedChange(EntityChange change) {
        return (change != null && change.interleaved());
    }
    
    private void updateInterleavingForSuspendedTransactions() {
        for (var suspendedTransaction : this.suspendedTransactions) {
            var dataForTransaction = this.transactionContextData.get(suspendedTransaction);
            if (dataForTransaction != null) {
                dataForTransaction.registerInterleavingWrite();
            }
        }
    }
    
    @Override
    public void onReadWriteConflict(EntityReadEvent event, TraceSimulationContext context) {
        var entity = event.entity();
        var dataStore = this.deploymentModel.getDataStoreForEntityType(entity.type()).orElseThrow(() -> new IllegalStateException("Entity type '" + entity.type() + "' is not assigned to a data store."));
        
        ConsistencyIssue<EntityReadEvent> issue;            
        if (dataStore.readWriteConflictBehavior() == ReadWriteConflictBehavior.STALE_READ) {
            issue = new StaleReadIssue(entity, event);
        } else {
            issue = new PotentialDeadlockIssue(entity, event);
        }
        
        this.foundIssues.add(issue);
    }
            
    @Override
    public void onCommittedWrite(EntityWriteEvent event, TraceSimulationContext context) {
        this.committedWrites.add(event);
    }
    
    @Override
    public void onRevertedWrite(EntityWriteEvent event, TraceSimulationContext context) {
        this.revertedWrites.add(event);
    }
    
    @Override
    public void onWriteWriteConflict(EntityWriteEvent event, TraceSimulationContext context) {
        // If the write conflicts with another transaction, raise an appropriate issue
        var entity = event.entity();
        var issue = new WriteConflictIssue(entity, event);
        this.foundIssues.add(issue);
        this.revertedWrites.add(event);
    }
    
    @Override
    public void onTransactionSuspend(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        if (this.checkForInterleavingAccesses) {
            this.suspendedTransactions.add(transaction);
        }
    }
    
    @Override
    public void onTransactionResume(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        if (this.checkForInterleavingAccesses) { 
            this.suspendedTransactions.remove(transaction);
        }
    }
    
    private static class TransactionData {                
        
        private final Map<Entity, EntityChange> entityChanges = new HashMap<>();
                
        public EntityChange existingChangeForEntity(Entity entity) {
            return this.entityChanges.get(entity);
        }
        
        public void registerWrite(EntityWriteEvent event) {
            var writtenEntity = event.entity();
            this.entityChanges.computeIfAbsent(writtenEntity, EntityChange::new);
            
            if (writtenEntity.hasRoot()) {
                var rootType = writtenEntity.type().rootType();
                if (rootType == null) {
                    throw new TraceProcessingException(event, "Entity type '" + writtenEntity.type() + "' does not have a root type.");
                }
                
                var rootEntity = new Entity(rootType, writtenEntity.rootId());
                this.entityChanges.computeIfAbsent(rootEntity, EntityChange::new);
            }
        }
        
        public void registerInterleavingWrite() {
            this.entityChanges.forEach((entity, entityChange) -> entityChange.setInterleaved());
        }
        
    }
        
    private static class EntityChange {
                
        private boolean interleaved;
        
        public EntityChange(Entity entity) {
            this.interleaved = false;
        }
        
        public boolean interleaved() {
            return this.interleaved;
        }
        
        public void setInterleaved() {
            this.interleaved = true;
        }        
        
    }
        
}
