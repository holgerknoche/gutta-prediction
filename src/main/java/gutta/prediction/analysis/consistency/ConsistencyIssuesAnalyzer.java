package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.Entity;
import gutta.prediction.domain.EntityType;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.simulation.TraceSimulationContext;
import gutta.prediction.simulation.TraceSimulationListener;
import gutta.prediction.simulation.TraceSimulator;
import gutta.prediction.simulation.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ConsistencyIssuesAnalyzer implements TraceSimulationListener {
    
    private final Map<Transaction, Set<Entity>> pendingChangesPerTransaction = new HashMap<>();
    
    private final Map<Entity, Transaction> pendingEntitiesToTransaction = new HashMap<>(); 
    
    private final List<ConsistencyIssue> foundIssues = new ArrayList<>();
    
    public List<ConsistencyIssue> findConsistencyIssues(EventTrace trace, DeploymentModel deploymentModel) {        
        new TraceSimulator(deploymentModel)
        .addListener(this)
        .processEvents(trace);
        
        return this.foundIssues;
    }
        
    @Override
    public void onTransactionCommit(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        System.out.println("Commit TX " + context.currentTransaction());
        
        // TODO Handle pending changes
        
        // TODO Auto-generated method stub
        TraceSimulationListener.super.onTransactionCommit(event, transaction, context);
    }
    
    @Override
    public void onTransactionAbort(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
        System.out.println("Abort TX " + context.currentTransaction());
        
        // TODO Handle pending changes
        
        // TODO Auto-generated method stub
        TraceSimulationListener.super.onTransactionAbort(event, transaction, context);
    }
    
    @Override
    public void onEntityReadEvent(EntityReadEvent event, TraceSimulationContext context) {
        System.out.println("Read entity " + event.entityType() + "-" + event.entityIdentifier() + " in TX " + context.currentTransaction());
        
        var currentTransaction = context.currentTransaction();
        if (currentTransaction == null) {
            return;
        }
        
        var entityType = new EntityType(event.entityType());
        var entity = new Entity(entityType, event.entityIdentifier());

        var changingTransaction = this.pendingEntitiesToTransaction.get(entity);
        if (changingTransaction != null && !(changingTransaction.equals(currentTransaction))) {
            System.out.println("Stale read of entity " + entity);
        }
    }
    
    @Override
    public void onEntityWriteEvent(EntityWriteEvent event, TraceSimulationContext context) {
        System.out.println("Write entity " + event.entityType() + "-" + event.entityIdentifier() + " in TX " + context.currentTransaction());

        var currentTransaction = context.currentTransaction();
        if (currentTransaction == null) {
            return;
        }
        
        var entityType = new EntityType(event.entityType());
        var entity = new Entity(entityType, event.entityIdentifier());

        // TODO Check for write-write conflicts
        
        
        var changedEntitiesInTransaction = this.pendingChangesPerTransaction.computeIfAbsent(currentTransaction, tx -> new HashSet<Entity>());
        changedEntitiesInTransaction.add(entity);
        
        this.pendingEntitiesToTransaction.put(entity, currentTransaction);
    }         

}
