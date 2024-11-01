package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DataStore;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.Entity;
import gutta.prediction.domain.EntityType;
import gutta.prediction.domain.ReadWriteConflictBehavior;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.TransactionPropagation;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ExplicitTransactionAbortEvent;
import gutta.prediction.event.ImplicitTransactionAbortEvent;
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

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for the class {@link ConsistencyIssuesAnalyzer}.
 */
class ConsistencyIssuesAnalyzerTest {
    
    /**
     * Test case: An empty trace has no issues.
     */
    @Test
    void emptyTrace() {
        var trace = EventTrace.of();
        
        var deploymentModel = new DeploymentModel.Builder()
                .build();
        
        var analyzer = new ConsistencyIssuesAnalyzer();
        var result = analyzer.analyzeTrace(trace, deploymentModel);
        
        var expectedResult = new ConsistencyAnalyzerResult(Set.of(), Set.of(), Set.of());
        
        assertEquals(expectedResult, result);
    }
    
    /**
     * Test case: If an entity is changed in an uncommitted transaction, and the same entity is read in a nested transaction, a "stale read" issue is created given the corresponding behavior of the data store.
     */
    @Test
    void staleRead() {
        var traceId = 1234;
        var location = new ObservedLocation("test", 1, 0);
        var entityType = new EntityType("et1");
        var entity = new Entity(entityType, "e1");
        
        var committedEvent = new EntityWriteEvent(traceId, 250, location, entity); 
        var conflictCausingEvent = new EntityReadEvent(traceId, 500, location, entity);
        
        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location, "uc"),
                new TransactionStartEvent(traceId, 200, location, "tx1"),
                committedEvent,                
                new ServiceCandidateInvocationEvent(traceId, 300, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 300, location, "sc1"),
                new TransactionStartEvent(traceId, 400, location, "tx2"),
                conflictCausingEvent,
                new TransactionCommitEvent(traceId, 700, location, "tx2"),
                new ServiceCandidateExitEvent(traceId, 800, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 800, location, "sc1"),
                new TransactionCommitEvent(traceId, 900, location, "tx1"),
                new UseCaseEndEvent(traceId, 1000, location, "uc")
                );
        
        var useCase = new UseCase("uc");        
        var serviceCandidate = new ServiceCandidate("sc1", TransactionBehavior.NOT_SUPPORTED);
        var dataStore = new DataStore("ds", ReadWriteConflictBehavior.STALE_READ);
        
        var component = new Component("c1");       
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCaseToComponent(useCase, component)
                .assignServiceCandidateToComponent(serviceCandidate, component)
                .assignEntityTypeToComponent(entityType, component)
                .assignEntityTypeToDataStore(entityType, dataStore)
                .build();
        
        var analyzer = new ConsistencyIssuesAnalyzer();
        var result = analyzer.analyzeTrace(trace, deploymentModel);
        
        var expectedResult = new ConsistencyAnalyzerResult(Set.of(new StaleReadIssue(entity, conflictCausingEvent)), Set.of(committedEvent), Set.of());
        
        assertEquals(expectedResult, result);
    }
    
    /**
     * Test case: If an entity is changed in an uncommitted transaction, and a read to the same entity occurs outside of a transaction, a "stale read" is raised.
     */
    @Test
    void staleReadWithoutSurroundingTransaction() {
        var traceId = 1234;
        var location = new ObservedLocation("test", 1, 0);
        var entityType = new EntityType("et1");
        var entity = new Entity(entityType, "e1");

        var committedEvent = new EntityWriteEvent(traceId, 250, location, entity); 
        var conflictCausingEvent = new EntityReadEvent(traceId, 500, location, entity);

        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location, "uc"),
                new TransactionStartEvent(traceId, 200, location, "tx1"),
                committedEvent,                
                new ServiceCandidateInvocationEvent(traceId, 300, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 300, location, "sc1"),
                conflictCausingEvent,
                new ServiceCandidateExitEvent(traceId, 800, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 800, location, "sc1"),
                new TransactionCommitEvent(traceId, 900, location, "tx1"),
                new UseCaseEndEvent(traceId, 1000, location, "uc")
                );

        var useCase = new UseCase("uc");        
        var serviceCandidate = new ServiceCandidate("sc1", TransactionBehavior.NOT_SUPPORTED);
        var dataStore = new DataStore("ds", ReadWriteConflictBehavior.STALE_READ);
        
        var component = new Component("c1");       
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCaseToComponent(useCase, component)
                .assignServiceCandidateToComponent(serviceCandidate, component)
                .assignEntityTypeToComponent(entityType, component)
                .assignEntityTypeToDataStore(entityType, dataStore)
                .build();
        
        var analyzer = new ConsistencyIssuesAnalyzer();
        var result = analyzer.analyzeTrace(trace, deploymentModel);
        
        var expectedResult = new ConsistencyAnalyzerResult(Set.of(new StaleReadIssue(entity, conflictCausingEvent)), Set.of(committedEvent), Set.of());
        
        assertEquals(expectedResult, result);
    }
    
    /**
     * Test case: If an entity is changed in an uncommitted transaction, and the same entity is read in a nested transaction, a "potential deadlock" issue is created given the corresponding behavior of the data store.
     */
    @Test
    void potentialDeadlockOnRead() {
        var traceId = 1234;
        var location = new ObservedLocation("test", 1, 0);
        var entityType = new EntityType("et1");
        var entity = new Entity(entityType, "e1");
        
        var committedEvent = new EntityWriteEvent(traceId, 250, location, entity); 
        var conflictCausingEvent = new EntityReadEvent(traceId, 500, location, entity);
        
        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location, "uc"),
                new TransactionStartEvent(traceId, 200, location, "tx1"),
                committedEvent,                
                new ServiceCandidateInvocationEvent(traceId, 300, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 300, location, "sc1"),
                new TransactionStartEvent(traceId, 400, location, "tx2"),
                conflictCausingEvent,
                new TransactionCommitEvent(traceId, 700, location, "tx2"),
                new ServiceCandidateExitEvent(traceId, 800, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 800, location, "sc1"),
                new TransactionCommitEvent(traceId, 900, location, "tx1"),
                new UseCaseEndEvent(traceId, 1000, location, "uc")
                );
        
        var useCase = new UseCase("uc");        
        var serviceCandidate = new ServiceCandidate("sc1", TransactionBehavior.NOT_SUPPORTED);
        var dataStore = new DataStore("ds", ReadWriteConflictBehavior.BLOCK);
        
        var component = new Component("c1");       
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCaseToComponent(useCase, component)
                .assignServiceCandidateToComponent(serviceCandidate, component)
                .assignEntityTypeToComponent(entityType, component)
                .assignEntityTypeToDataStore(entityType, dataStore)
                .build();
        
        var analyzer = new ConsistencyIssuesAnalyzer();
        var result = analyzer.analyzeTrace(trace, deploymentModel);
        
        var expectedResult = new ConsistencyAnalyzerResult(Set.of(new PotentialDeadlockIssue(entity, conflictCausingEvent)), Set.of(committedEvent), Set.of());
        
        assertEquals(expectedResult, result);
    }
    
    /**
     * Test case: If an entity is changed in an uncommitted transaction, and the same entity is written again in a nested transaction, a "write conflict" issue is created.
     */
    @Test
    void writeConflict() {
        var traceId = 1234;
        var location = new ObservedLocation("test", 1, 0);
        var entityType = new EntityType("et1");
        var entity = new Entity(entityType, "e1");
        
        var committedEvent = new EntityWriteEvent(traceId, 250, location, entity);
        var conflictCausingEvent = new EntityWriteEvent(traceId, 500, location, entity); 
        
        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location, "uc"),
                new TransactionStartEvent(traceId, 200, location, "tx1"),
                committedEvent,                
                new ServiceCandidateInvocationEvent(traceId, 300, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 300, location, "sc1"),
                new TransactionStartEvent(traceId, 400, location, "tx2"),
                conflictCausingEvent,
                new TransactionCommitEvent(traceId, 700, location, "tx2"),
                new ServiceCandidateExitEvent(traceId, 800, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 800, location, "sc1"),
                new TransactionCommitEvent(traceId, 900, location, "tx1"),
                new UseCaseEndEvent(traceId, 1000, location, "uc")
                );
        
        var useCase = new UseCase("uc");        
        var serviceCandidate = new ServiceCandidate("sc1", TransactionBehavior.NOT_SUPPORTED);
        
        var component = new Component("c1");       
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCaseToComponent(useCase, component)
                .assignServiceCandidateToComponent(serviceCandidate, component)
                .assignEntityTypeToComponent(entityType, component)
                .build();
        
        var analyzer = new ConsistencyIssuesAnalyzer();
        var result = analyzer.analyzeTrace(trace, deploymentModel);
        
        var expectedResult = new ConsistencyAnalyzerResult(Set.of(new WriteConflictIssue(entity, conflictCausingEvent)), Set.of(committedEvent), Set.of(conflictCausingEvent));
        
        assertEquals(expectedResult, result);
    }
    
    /**
     * Test case: If a nested, but unrelated transaction is committed and the surrounding transaction is aborted, the appropriate changes are returned.
     */
    @Test
    void nestedCommitAndOuterAbort() {
        var traceId = 1234;
        var location = new ObservedLocation("test", 1, 0);
        var entityType = new EntityType("et1");
        
        var entity1 = new Entity(entityType, "e1");
        var entity2 = new Entity(entityType, "e2");
        
        var abortedEvent = new EntityWriteEvent(traceId, 250, location, entity1);
        var committedEvent = new EntityWriteEvent(traceId, 500, location, entity2); 
        
        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location, "uc"),
                new TransactionStartEvent(traceId, 200, location, "tx1"),
                abortedEvent,                
                new ServiceCandidateInvocationEvent(traceId, 300, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 300, location, "sc1"),
                new TransactionStartEvent(traceId, 400, location, "tx2"),
                committedEvent,
                new TransactionCommitEvent(traceId, 700, location, "tx2"),
                new ServiceCandidateExitEvent(traceId, 800, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 800, location, "sc1"),
                new ExplicitTransactionAbortEvent(traceId, 900, location, "tx1"),
                new UseCaseEndEvent(traceId, 1000, location, "uc")
                );
        
        var useCase = new UseCase("uc");        
        var serviceCandidate = new ServiceCandidate("sc1", TransactionBehavior.NOT_SUPPORTED);
        
        var component = new Component("c1");       
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCaseToComponent(useCase, component)
                .assignServiceCandidateToComponent(serviceCandidate, component)
                .assignEntityTypeToComponent(entityType, component)
                .build();
        
        var analyzer = new ConsistencyIssuesAnalyzer();
        var result = analyzer.analyzeTrace(trace, deploymentModel);
        
        var expectedResult = new ConsistencyAnalyzerResult(Set.of(), Set.of(committedEvent), Set.of(abortedEvent));
        
        assertEquals(expectedResult, result);
    }
    
    /**
     * Test case: Writes in subordinate transactions are also recorded.
     */
    @Test
    void writesInSubordinateTransaction() {
        var traceId = 1234;
        
        var location1 = new ObservedLocation("test1", 123, 1);
        var location2 = new ObservedLocation("test2", 123, 1);
        
        var entityType = new EntityType("et1");
        var entity1 = new Entity(entityType, "1");
        var entity2 = new Entity(entityType, "2"); 
        
        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location1, "uc"),
                new TransactionStartEvent(traceId, 200, location1, "tx1"),
                new EntityWriteEvent(traceId, 300, location1, entity1),
                new ServiceCandidateInvocationEvent(traceId, 400, location1, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 500, location2, "sc1"),
                new EntityWriteEvent(traceId, 600, location2, entity2),
                new ServiceCandidateExitEvent(traceId, 700, location2, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 800, location1, "sc1"),
                new TransactionCommitEvent(traceId, 900, location1, "tx1"),
                new UseCaseEndEvent(traceId, 1000, location1, "uc")                
                );
        
        var useCase = new UseCase("uc");        
        var serviceCandidate = new ServiceCandidate("sc1", TransactionBehavior.REQUIRED);
        
        var component1 = new Component("c1");       
        var component2 = new Component("c2");
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCaseToComponent(useCase, component1)
                .assignServiceCandidateToComponent(serviceCandidate, component2)
                .assignEntityTypeToComponent(entityType, component1)
                .addSymmetricRemoteConnection(component1, component2, 0, TransactionPropagation.SUBORDINATE)
                .build();
        
        var analyzer = new ConsistencyIssuesAnalyzer(CheckCrossComponentAccesses.NO);
        var result = analyzer.analyzeTrace(trace, deploymentModel);
        
        var expectedCommittedWrite1 = new EntityWriteEvent(traceId, 300, location1, entity1);
        var expectedCommittedWrite2 = new EntityWriteEvent(traceId, 600, location2, entity2);
                
        var expectedResult = new ConsistencyAnalyzerResult(Set.of(), Set.of(expectedCommittedWrite1, expectedCommittedWrite2), Set.of());
        
        assertEquals(expectedResult, result);
    }
    
    /**
     * Test case: Commit of an implicitly demarcated transaction.
     */
    @Test
    void commitOfImplicitTransaction() {
        var traceId = 1234;
        var location = new ObservedLocation("test", 1, 0);
        var entityType = new EntityType("et1");
        
        var entity1 = new Entity(entityType, "e1");
        
        var committedEvent = new EntityWriteEvent(traceId, 200, location, entity1);
        
        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 0, location, "uc"),
                new ServiceCandidateInvocationEvent(traceId, 100, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 100, location, "sc1", true, "tx1"),
                committedEvent,
                new ServiceCandidateExitEvent(traceId, 900, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 900, location, "sc1"),
                new UseCaseEndEvent(traceId, 1000, location, "uc")
                );

        var useCase = new UseCase("uc");        
        var serviceCandidate = new ServiceCandidate("sc1", TransactionBehavior.REQUIRED);
        
        var component = new Component("c1");       

        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCaseToComponent(useCase, component)
                .assignServiceCandidateToComponent(serviceCandidate, component)
                .assignEntityTypeToComponent(entityType, component)
                .build();
        
        var analyzer = new ConsistencyIssuesAnalyzer();
        var result = analyzer.analyzeTrace(trace, deploymentModel);
        
        var expectedResult = new ConsistencyAnalyzerResult(Set.of(), Set.of(committedEvent), Set.of());
        
        assertEquals(expectedResult, result);
    }
    
    /**
     * Test case: Abort of an implicitly demarcated transaction.
     */
    @Test
    void abortOfImplicitTransaction() {
        var traceId = 1234;
        var location = new ObservedLocation("test", 1, 0);
        var entityType = new EntityType("et1");
        
        var entity1 = new Entity(entityType, "e1");
        
        var abortedEvent = new EntityWriteEvent(traceId, 200, location, entity1);
        
        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 0, location, "uc"),
                new ServiceCandidateInvocationEvent(traceId, 100, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 100, location, "sc1", true, "tx1"),
                abortedEvent,
                new ImplicitTransactionAbortEvent(traceId, 400, location, "tx1", "cause"),
                new ServiceCandidateExitEvent(traceId, 900, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 900, location, "sc1"),
                new UseCaseEndEvent(traceId, 1000, location, "uc")
                );

        var useCase = new UseCase("uc");        
        var serviceCandidate = new ServiceCandidate("sc1", TransactionBehavior.REQUIRED);
        
        var component = new Component("c1");       

        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCaseToComponent(useCase, component)
                .assignServiceCandidateToComponent(serviceCandidate, component)
                .assignEntityTypeToComponent(entityType, component)
                .build();
        
        var analyzer = new ConsistencyIssuesAnalyzer();
        var result = analyzer.analyzeTrace(trace, deploymentModel);
        
        var expectedResult = new ConsistencyAnalyzerResult(Set.of(), Set.of(), Set.of(abortedEvent));
        
        assertEquals(expectedResult, result);   
    }
    
    /**
     * Test case: An auto-committed write (i.e., without a surrounding transaction), is recorded.
     */
    @Test
    void autoCommittedWrite() {
        var traceId = 1234;
        var location = new ObservedLocation("test", 1, 0);
        var entityType = new EntityType("et1");
        
        var entity = new Entity(entityType, "e1");

        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 0, location, "uc"),
                new EntityWriteEvent(traceId, 200, location, entity),
                new UseCaseEndEvent(traceId, 1000, location, "uc")
                );

        var useCase = new UseCase("uc");
        var component = new Component("c1");
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCaseToComponent(useCase, component)
                .assignEntityTypeToComponent(entityType, component)
                .build();
        
        var analyzer = new ConsistencyIssuesAnalyzer();
        var result = analyzer.analyzeTrace(trace, deploymentModel);
        
        var expectedCommittedWrite = new EntityWriteEvent(traceId, 200, location, entity); 
        
        var expectedResult = new ConsistencyAnalyzerResult(Set.of(), Set.of(expectedCommittedWrite), Set.of());
        
        assertEquals(expectedResult, result);
    }
    
    /**
     * Test case: A write conflict aborts the current transaction.
     */
    @Test
    void writeConflictAbortsTransaction() {
        var traceId = 1234;
        var location = new ObservedLocation("test", 1, 0);
        var entityType = new EntityType("et1");
        
        var entity1 = new Entity(entityType, "e1");
        var entity2 = new Entity(entityType, "e2");

        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 0, location, "uc"),
                new TransactionStartEvent(traceId, 100, location, "tx1"),
                new EntityWriteEvent(traceId, 200, location, entity1),
                new ServiceCandidateInvocationEvent(traceId, 300, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 400, location, "sc1"),
                new EntityWriteEvent(traceId, 500, location, entity1),
                new EntityWriteEvent(traceId, 600, location, entity2),
                new ServiceCandidateExitEvent(traceId, 600, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 700, location, "sc1"),
                new TransactionCommitEvent(traceId, 800, location, "tx1"),
                new UseCaseEndEvent(traceId, 900, location, "uc")
                );

        var useCase = new UseCase("uc");
        var serviceCandidate = new ServiceCandidate("sc1", TransactionBehavior.REQUIRES_NEW);
        var component = new Component("c1");
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCaseToComponent(useCase, component)
                .assignServiceCandidateToComponent(serviceCandidate, component)
                .assignEntityTypeToComponent(entityType, component)
                .build();
        
        var analyzer = new ConsistencyIssuesAnalyzer();
        var result = analyzer.analyzeTrace(trace, deploymentModel);
        
        var expectedIssue = new WriteConflictIssue(entity1, new EntityWriteEvent(traceId, 500, location, entity1));
        var expectedCommittedWrite = new EntityWriteEvent(traceId, 200, location, entity1);
        var expectedRevertedWrite1 = new EntityWriteEvent(traceId, 500, location, entity1);
        var expectedRevertedWrite2 = new EntityWriteEvent(traceId, 600, location, entity2);
        
        var expectedResult = new ConsistencyAnalyzerResult(Set.of(expectedIssue), Set.of(expectedCommittedWrite), Set.of(expectedRevertedWrite1, expectedRevertedWrite2));
        
        assertEquals(expectedResult, result);
    }        

}
