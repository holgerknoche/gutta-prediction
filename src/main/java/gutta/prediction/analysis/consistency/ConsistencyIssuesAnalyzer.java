package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.Entity;
import gutta.prediction.domain.ReadWriteConflictBehavior;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.simulation.TraceSimulationContext;
import gutta.prediction.simulation.TraceSimulationListener;
import gutta.prediction.simulation.Transaction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static gutta.prediction.simulation.TraceSimulator.runSimulationOf;

class ConsistencyIssuesAnalyzer implements TraceSimulationListener {
    
    private final Map<Transaction, Set<EntityWriteEvent>> pendingWritesPerTransaction = new HashMap<>();
    
    private final Map<Entity, Transaction> pendingEntitiesToTransaction = new HashMap<>(); 
    
    private final Set<ConsistencyIssue<?>> foundIssues = new HashSet<>();
    
    private final Set<EntityWriteEvent> committedWrites = new HashSet<>();
    
    private final Set<EntityWriteEvent> abortedWrites = new HashSet<>();
    
    private DeploymentModel deploymentModel;
    
    public ConsistencyAnalyzerResult analyzeTrace(EventTrace trace, DeploymentModel deploymentModel) {
        this.deploymentModel = deploymentModel;
        
        runSimulationOf(trace, deploymentModel, this);
        
        return new ConsistencyAnalyzerResult(this.foundIssues, this.committedWrites, this.abortedWrites);
    }
        
    @Override
    public void onTransactionCommit(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        this.handleCompletionOfTransaction(transaction, this.committedWrites::addAll);
    }
    
    @Override
    public void onTransactionAbort(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        this.handleCompletionOfTransaction(transaction, this.abortedWrites::addAll);
    }
    
    private void handleCompletionOfTransaction(Transaction transaction, Consumer<Set<EntityWriteEvent>> eventConsumer) {
        // Send the pending write events to the given consumer and remove them from the map
        var pendingWrites = this.pendingWritesPerTransaction.get(transaction);
        if (pendingWrites == null) {
            return;
        }
        
        eventConsumer.accept(pendingWrites);
        this.pendingWritesPerTransaction.remove(transaction);
        
        // Remove the changed entities from the appropriate map
        var changedEntities = pendingWrites.stream().map(EntityWriteEvent::entity).collect(Collectors.toSet());
        changedEntities.forEach(this.pendingEntitiesToTransaction::remove);
    }
        
    @Override
    public void onEntityReadEvent(EntityReadEvent event, TraceSimulationContext context) {
        var currentTransaction = context.currentTransaction();
        if (currentTransaction == null) {
            return;
        }
                
        var entity = event.entity();
        var dataStore = this.deploymentModel.getDataStoreForEntityType(entity.type()).orElseThrow(() -> new IllegalStateException("Entity type '" + entity.type() + "' is not assigned to a data store."));

        if (this.hasConflict(entity, currentTransaction)) {

            ConsistencyIssue<EntityReadEvent> issue;            
            if (dataStore.readWriteConflictBehavior() == ReadWriteConflictBehavior.STALE_READ) {
                issue = new StaleReadIssue(entity, event);
            } else {
                issue = new PotentialDeadlockIssue(entity, event);
            }
            
            this.foundIssues.add(issue);
        }
    }
    
    private boolean hasConflict(Entity entity, Transaction currentTransaction) {
        var changingTransaction = this.pendingEntitiesToTransaction.get(entity);
        return (changingTransaction != null && !(changingTransaction.equals(currentTransaction)));     
    }
    
    @Override
    public void onEntityWriteEvent(EntityWriteEvent event, TraceSimulationContext context) {
        var currentTransaction = context.currentTransaction();
        if (currentTransaction == null) {
            return;
        }
                
        var entity = event.entity();

        if (this.hasConflict(entity, currentTransaction)) {
            var issue = new WriteConflictIssue(entity, event);
            this.foundIssues.add(issue);
        } else {       
            var pendingWritesInTransaction = this.pendingWritesPerTransaction.computeIfAbsent(currentTransaction, tx -> new HashSet<EntityWriteEvent>());
            pendingWritesInTransaction.add(event);
        
            this.pendingEntitiesToTransaction.put(entity, currentTransaction);
        }
    }         

}
