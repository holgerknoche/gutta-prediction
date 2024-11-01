package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.Entity;
import gutta.prediction.domain.EntityType;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.TransactionPropagation;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ExplicitTransactionAbortEvent;
import gutta.prediction.event.ObservedLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.SyntheticLocation;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for the class {@link ConsistencyIssuesAnalysis}.
 */
class ConsistencyIssuesAnalysisTest {
    
    /**
     * Test case: Changes are committed instead of reverted because they have been moved to a separate transaction.
     */
    @Test
    void changesNotRevertedDueToTransactionSeparation() {
        var location = new ObservedLocation("test", 1234, 1);
        var traceId = 1234;
        
        var entityType = new EntityType("et1");
        var entity1 = new Entity(entityType, "e1");
        var entity2 = new Entity(entityType, "e2");
        var entity3 = new Entity(entityType, "e3");
        
        var inputTrace = EventTrace.of(
                new UseCaseStartEvent(traceId, 0, location, "uc1"),
                new TransactionStartEvent(traceId, 100, location, "tx1"),
                new EntityWriteEvent(traceId, 200, location, entity1),
                new ServiceCandidateInvocationEvent(traceId, 300, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 300, location, "sc1"),
                new EntityWriteEvent(traceId, 400, location, entity2),
                new EntityWriteEvent(traceId, 500, location, entity3),
                new ServiceCandidateExitEvent(traceId, 800, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 800, location, "sc1"),
                new ExplicitTransactionAbortEvent(traceId, 900, location, "tx1"),
                new UseCaseEndEvent(traceId, 1000, location, "uc1")
                );
        
        var useCase = new UseCase("uc1");
        var serviceCandidate = new ServiceCandidate("sc1", TransactionBehavior.REQUIRED);
        
        var component1 = new Component("c1");
        var component2 = new Component("c2");
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCaseToComponent(useCase, component1)
                .assignServiceCandidateToComponent(serviceCandidate, component1)
                .build();
        
        var modifiedDeploymentModel = deploymentModel.applyModifications()
                .assignServiceCandidateToComponent(serviceCandidate, component2)
                .addSymmetricRemoteConnection(component1, component2, 0, TransactionPropagation.NONE)
                .build();
        
        var analysis = new ConsistencyIssuesAnalysis(CheckCrossComponentAccesses.NO);
        var result = analysis.analyzeTrace(inputTrace, deploymentModel, modifiedDeploymentModel);
        
        var syntheticLocation = new SyntheticLocation(0);
        
        var expectedCommittedWrite1 = new EntityWriteEvent(traceId, 400, syntheticLocation, entity2);
        var expectedCommittedWrite2 = new EntityWriteEvent(traceId, 500, syntheticLocation, entity3);
        
        var expectedUnchangedRevertedWrite = new EntityWriteEvent(traceId, 200, location, entity1);

        var expectedResult = new ConsistencyAnalysisResult(0, 0, Set.of(), Set.of(), Set.of(), Set.of(expectedCommittedWrite1, expectedCommittedWrite2), Set.of(), Set.of(), Set.of(expectedUnchangedRevertedWrite));
        
        assertEquals(expectedResult, result);
    }
    
    /**
     * Test case: A write conflict is resolved by internalizing the component.
     */
    @Test
    void resolutionOfWriteConflict() {
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
                new ServiceCandidateEntryEvent(traceId, 400, location2, "sc1"),
                new EntityWriteEvent(traceId, 600, location2, entity1),
                new EntityWriteEvent(traceId, 700, location2, entity2),
                new ServiceCandidateExitEvent(traceId, 800, location2, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 800, location1, "sc1"),
                new TransactionCommitEvent(traceId, 1000, location1, "tx1"),
                new UseCaseEndEvent(traceId, 1100, location1, "uc")                
                );
        
        var useCase = new UseCase("uc");
        var serviceCandidate = new ServiceCandidate("sc1", TransactionBehavior.REQUIRED);
        
        var component1 = new Component("c1");
        var component2 = new Component("c2");
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCaseToComponent(useCase, component1)
                .assignServiceCandidateToComponent(serviceCandidate, component2)
                .addSymmetricRemoteConnection(component1, component2, 0, TransactionPropagation.NONE)
                .build();
        
        var modifiedDeploymentModel = deploymentModel.applyModifications()
                .addLocalConnection(component1, component2)
                .build();
        
        var analysis = new ConsistencyIssuesAnalysis(CheckCrossComponentAccesses.NO);
        var result = analysis.analyzeTrace(trace, deploymentModel, modifiedDeploymentModel);
     
        var expectedUnchangedCommittedWrite = new EntityWriteEvent(traceId, 300, location1, entity1);
        var expectedNowCommittedWrite1 = new EntityWriteEvent(traceId, 600, location1, entity1);
        var expectedNowCommittedWrite2 = new EntityWriteEvent(traceId, 700, location1, entity2);
        
        var expectedObsoleteIssue = new WriteConflictIssue(entity1, new EntityWriteEvent(traceId, 600, location2, entity1));                
        
        var expectedResult = new ConsistencyAnalysisResult(1, // One issue in the original trace
                0, // No issues in the modified trace
                Set.of(), // No new issues
                Set.of(expectedObsoleteIssue), // The write conflict is now obsolete
                Set.of(), // No unchanged issues
                Set.of(expectedNowCommittedWrite1, expectedNowCommittedWrite2), // Two now-committed writes
                Set.of(), // No now-reverted writes
                Set.of(expectedUnchangedCommittedWrite), // The first write is always committed 
                Set.of()); // No unchanged reverted writes
                
        assertEquals(expectedResult, result);
    }

}
