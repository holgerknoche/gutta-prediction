package gutta.prediction.simulation;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.Entity;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TraceSimulationContext {

    private final DeploymentModel deploymentModel;
        
    private final EventStream eventStream;
    
    private final Deque<StackEntry> stack = new ArrayDeque<>();
    
    private final Map<Transaction, Set<EntityWriteEvent>> pendingWritesPerTransaction = new HashMap<>();
    
    private final Map<Entity, Transaction> pendingEntitiesToTransaction = new HashMap<>(); 

    private ServiceCandidate currentServiceCandidate;
    
    private Component currentComponent;        
    
    private Location currentLocation;
    
    private Transaction currentTransaction;
    
    TraceSimulationContext(DeploymentModel deploymentModel, EventStream eventStream) {
        this.deploymentModel = deploymentModel;
        this.eventStream = eventStream;
    }
    
    public ServiceCandidate currentServiceCandidate() {
        return this.currentServiceCandidate;
    }
    
    void currentServiceCandidate(ServiceCandidate candidate) {
        this.currentServiceCandidate = candidate;
    }
    
    public Component currentComponent() {
        return this.currentComponent;
    }
    
    void currentComponent(Component component) {
        this.currentComponent = component;
    }
    
    public Location currentLocation() {
        return this.currentLocation;
    }
    
    void currentLocation(Location location) {
        this.currentLocation = location;
    }
    
    public Transaction currentTransaction() {
        return this.currentTransaction;
    }
    
    void currentTransaction(Transaction transaction) {
        this.currentTransaction = transaction;
    }
           
    public DeploymentModel deploymentModel() {
        return this.deploymentModel;
    }
        
    public MonitoringEvent lookahead(int amount) {
        return this.eventStream.lookahead(amount);
    }
    
    public MonitoringEvent lookback(int amount) {
        return this.eventStream.lookback(amount);
    }
    
    public StackEntry peek() {
        return this.stack.peek();
    }
    
    void pushCurrentState() {
        this.stack.push(new StackEntry(this.currentServiceCandidate, this.currentComponent, this.currentLocation, this.currentTransaction));
    }
    
    StackEntry popCurrentState() {
        var entry = this.stack.pop();
        
        this.currentServiceCandidate = entry.serviceCandidate();
        this.currentComponent = entry.component();
        this.currentLocation = entry.location();
        this.currentTransaction = entry.transaction();
        
        return entry;
    }
    
    Transaction getTransactionWithPendingWriteTo(Entity entity) {
        return this.pendingEntitiesToTransaction.get(entity);
    }
    
    void registerPendingWrite(EntityWriteEvent event) {
        var pendingWritesInTransaction = this.pendingWritesPerTransaction.computeIfAbsent(this.currentTransaction, tx -> new HashSet<EntityWriteEvent>());
        pendingWritesInTransaction.add(event);
        
        this.pendingEntitiesToTransaction.put(event.entity(), this.currentTransaction);
    }
    
    Set<EntityWriteEvent> getAndRemovePendingWritesFor(Transaction transaction) {
        var pendingWrites = this.pendingWritesPerTransaction.remove(transaction);
        if (pendingWrites == null) {
            return Set.of();
        }
        
        // Remove the changed entities from the appropriate map
        pendingWrites.stream()
                .map(EntityWriteEvent::entity)
                .forEach(this.pendingEntitiesToTransaction::remove);
        
        return pendingWrites;
    }
        
}
