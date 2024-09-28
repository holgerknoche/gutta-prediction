package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.Entity;
import gutta.prediction.domain.EntityType;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ProcessLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        var foundIssues = analyzer.findConsistencyIssues(trace, deploymentModel);
        
        assertEquals(List.of(), foundIssues);
    }
    
    /**
     * Test case: If an entity is changed in an uncommitted transaction, and the same entity is read in a nested transaction, a "stale read" issue is created.
     */
    @Test
    void staleRead() {
        var traceId = 1234;
        var location = new ProcessLocation("test", 1, 0);
        
        var conflictCausingEvent = new EntityReadEvent(traceId, 500, location, "et1", "e1");
        
        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location, "uc"),
                new TransactionStartEvent(traceId, 200, location, "tx1"),
                new EntityWriteEvent(traceId, 250, location, "et1", "e1"),                
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
                .assignUseCase(useCase, component)
                .assignServiceCandidate(serviceCandidate, component)
                .build();
        
        var analyzer = new ConsistencyIssuesAnalyzer();
        var foundIssues = analyzer.findConsistencyIssues(trace, deploymentModel);
        
        assertEquals(List.of(new StaleReadIssue(new Entity(new EntityType("et1"), "e1"), conflictCausingEvent)), foundIssues);
    }
    
    /**
     * Test case: If an entity is changed in an uncommitted transaction, and the same entity is written again in a nested transaction, a "write conflict" issue is created.
     */
    @Test
    void writeConflict() {
        var traceId = 1234;
        var location = new ProcessLocation("test", 1, 0);
        
        var conflictCausingEvent = new EntityWriteEvent(traceId, 500, location, "et1", "e1"); 
        
        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location, "uc"),
                new TransactionStartEvent(traceId, 200, location, "tx1"),
                new EntityWriteEvent(traceId, 250, location, "et1", "e1"),                
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
                .assignUseCase(useCase, component)
                .assignServiceCandidate(serviceCandidate, component)
                .build();
        
        var analyzer = new ConsistencyIssuesAnalyzer();
        var foundIssues = analyzer.findConsistencyIssues(trace, deploymentModel);
        
        assertEquals(List.of(new WriteConflictIssue(new Entity(new EntityType("et1"), "e1"), conflictCausingEvent)), foundIssues);
    }

}
