package gutta.prediction.simulation;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.Entity;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ObservedLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for the class {@link EntityAccessSimulatorWorker}.
 */
class EntityAccessSimulatorWorkerTest {
    
    /**
     * Test case: If a read-write conflict occurs, the appropriate handler method is invoked.
     */
    @Test
    void readWriteConflictInConcurrentTransactions() {
        var traceId = 1234;
        var location = new ObservedLocation("test", 1234, 1);
        
        var entity = new Entity("type", "1");
        
        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 10, location, "uc"),
                new TransactionStartEvent(traceId, 20, location, "tx1"),
                new EntityWriteEvent(traceId, 30, location, entity),
                new ServiceCandidateInvocationEvent(traceId, 40, location, "sc"),
                new ServiceCandidateEntryEvent(traceId, 50, location, "sc"),
                new EntityReadEvent(traceId, 60, location, entity),
                new ServiceCandidateExitEvent(traceId, 70, location, "sc"),
                new ServiceCandidateReturnEvent(traceId, 80, location, "sc"),
                new TransactionCommitEvent(traceId, 90, location, "tx1"),                
                new UseCaseEndEvent(traceId, 100, location, "uc")
                );
        
        var useCase = new UseCase("uc");
        var serviceCandidate = new ServiceCandidate("sc", TransactionBehavior.REQUIRES_NEW);
        
        var component = new Component("component");
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCaseToComponent(useCase, component)
                .assignServiceCandidateToComponent(serviceCandidate, component)
                .build();
        
        var listener = new ConflictListener();
        new EntityAccessSimulatorWorker(List.of(listener), trace, deploymentModel).processEvents();
        
        var expectedConflict = new Conflict(ConflictType.READ_WRITE, new EntityReadEvent(traceId, 60, location, entity));
        var expectedConflicts = List.of(expectedConflict);
        
        assertEquals(expectedConflicts, listener.encounteredConflicts());
    }
    
    /**
     * Test case: If a write-write conflict occurs, the appropriate handler method is invoked.
     */
    @Test
    void writeWriteConflictInConcurrentTransactions() {
        var traceId = 1234;
        var location = new ObservedLocation("test", 1234, 1);
        
        var entity = new Entity("type", "1");
        
        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 10, location, "uc"),
                new TransactionStartEvent(traceId, 20, location, "tx1"),
                new EntityWriteEvent(traceId, 30, location, entity),
                new ServiceCandidateInvocationEvent(traceId, 40, location, "sc"),
                new ServiceCandidateEntryEvent(traceId, 50, location, "sc"),
                new EntityWriteEvent(traceId, 60, location, entity),
                new ServiceCandidateExitEvent(traceId, 70, location, "sc"),
                new ServiceCandidateReturnEvent(traceId, 80, location, "sc"),
                new TransactionCommitEvent(traceId, 90, location, "tx1"),                
                new UseCaseEndEvent(traceId, 100, location, "uc")
                );
        
        var useCase = new UseCase("uc");
        var serviceCandidate = new ServiceCandidate("sc", TransactionBehavior.REQUIRES_NEW);
        
        var component = new Component("component");
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCaseToComponent(useCase, component)
                .assignServiceCandidateToComponent(serviceCandidate, component)
                .build();
        
        var listener = new ConflictListener();
        new EntityAccessSimulatorWorker(List.of(listener), trace, deploymentModel).processEvents();
        
        var expectedConflict = new Conflict(ConflictType.WRITE_WRITE, new EntityWriteEvent(traceId, 60, location, entity));
        var expectedConflicts = List.of(expectedConflict);
        
        assertEquals(expectedConflicts, listener.encounteredConflicts());
    }
    
    /**
     * Test case: If an entity is written during an asynchronous invocation, a read after the asynchronous commit is still considered a conflict.
     */
    @Test
    void readWriteConflictAfterAsynchronousInvocation() {
        var traceId = 1234;
        var location = new ObservedLocation("test", 1234, 1);
        
        var entity = new Entity("type", "1");
        
        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 10, location, "uc"),
                new ServiceCandidateInvocationEvent(traceId, 20, location, "sc"),
                new ServiceCandidateEntryEvent(traceId, 30, location, "sc"),
                new EntityWriteEvent(traceId, 40, location, entity),
                new ServiceCandidateExitEvent(traceId, 50, location, "sc"),
                new ServiceCandidateReturnEvent(traceId, 60, location, "sc"),
                new EntityReadEvent(traceId, 70, location, entity),                
                new UseCaseEndEvent(traceId, 100, location, "uc")
                );
        
        var useCase = new UseCase("uc");
        var serviceCandidate = new ServiceCandidate("sc", TransactionBehavior.REQUIRED, true);
        
        var component = new Component("component");
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCaseToComponent(useCase, component)
                .assignServiceCandidateToComponent(serviceCandidate, component)
                .build();
        
        var listener = new ConflictListener();
        new EntityAccessSimulatorWorker(List.of(listener), trace, deploymentModel).processEvents();
        
        var expectedConflict = new Conflict(ConflictType.READ_WRITE, new EntityReadEvent(traceId, 70, location, entity));
        var expectedConflicts = List.of(expectedConflict);
        
        assertEquals(expectedConflicts, listener.encounteredConflicts());
    }
    
    /**
     * Test case: If an entity is written during an asynchronous invocation, a read after the asynchronous commit is still considered a conflict.
     */
    @Test
    void writeWriteConflictAfterAsynchronousInvocation() {
        var traceId = 1234;
        var location = new ObservedLocation("test", 1234, 1);
        
        var entity = new Entity("type", "1");
        
        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 10, location, "uc"),
                new ServiceCandidateInvocationEvent(traceId, 20, location, "sc"),
                new ServiceCandidateEntryEvent(traceId, 30, location, "sc"),
                new EntityWriteEvent(traceId, 40, location, entity),
                new ServiceCandidateExitEvent(traceId, 50, location, "sc"),
                new ServiceCandidateReturnEvent(traceId, 60, location, "sc"),
                new EntityWriteEvent(traceId, 70, location, entity),                
                new UseCaseEndEvent(traceId, 100, location, "uc")
                );
        
        var useCase = new UseCase("uc");
        var serviceCandidate = new ServiceCandidate("sc", TransactionBehavior.REQUIRED, true);
        
        var component = new Component("component");
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCaseToComponent(useCase, component)
                .assignServiceCandidateToComponent(serviceCandidate, component)
                .build();
        
        var listener = new ConflictListener();
        new EntityAccessSimulatorWorker(List.of(listener), trace, deploymentModel).processEvents();
        
        var expectedConflict = new Conflict(ConflictType.WRITE_WRITE, new EntityWriteEvent(traceId, 70, location, entity));
        var expectedConflicts = List.of(expectedConflict);
        
        assertEquals(expectedConflicts, listener.encounteredConflicts());
    }
    
    private static class ConflictListener implements TraceSimulationListener {
        
        private final List<Conflict> encounteredConflicts = new ArrayList<>();
        
        public List<Conflict> encounteredConflicts() {
            return this.encounteredConflicts;
        }
        
        @Override
        public void onReadWriteConflict(EntityReadEvent event, TraceSimulationContext context) {
            var conflict = new Conflict(ConflictType.READ_WRITE, event);
            this.encounteredConflicts.add(conflict);
        }
        
        @Override
        public void onWriteWriteConflict(EntityWriteEvent event, TraceSimulationContext context) {
            var conflict = new Conflict(ConflictType.WRITE_WRITE, event);
            this.encounteredConflicts.add(conflict);
        }
        
    }
    
    private record Conflict(ConflictType type, MonitoringEvent event) {}
    
    private enum ConflictType {
        READ_WRITE,
        WRITE_WRITE
    }

}
