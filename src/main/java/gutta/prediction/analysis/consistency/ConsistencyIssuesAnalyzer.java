package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ReadWriteConflictBehavior;
import gutta.prediction.event.EntityAccessEvent;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.simulation.TraceProcessingException;
import gutta.prediction.simulation.TraceSimulationContext;
import gutta.prediction.simulation.TraceSimulationListener;
import gutta.prediction.simulation.TraceSimulationMode;

import java.util.HashSet;
import java.util.Set;

import static gutta.prediction.simulation.TraceSimulator.runSimulationOf;

class ConsistencyIssuesAnalyzer implements TraceSimulationListener {
    
    private final boolean checkForCrossComponentAccesses;
        
    private final Set<ConsistencyIssue<?>> foundIssues = new HashSet<>();
    
    private final Set<EntityWriteEvent> committedWrites = new HashSet<>();
    
    private final Set<EntityWriteEvent> revertedWrites = new HashSet<>();
    
    private DeploymentModel deploymentModel;
    
    ConsistencyIssuesAnalyzer() {
        this(CheckCrossComponentAccesses.YES);
    }
    
    public ConsistencyIssuesAnalyzer(CheckCrossComponentAccesses checkCrossComponentAccesses) {
        this.checkForCrossComponentAccesses = (checkCrossComponentAccesses == CheckCrossComponentAccesses.YES);
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
    
}
