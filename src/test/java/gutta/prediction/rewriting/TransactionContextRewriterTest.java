package gutta.prediction.rewriting;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.TransactionPropagation;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ImplicitTransactionAbortEvent;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ProcessLocation;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for the class {@link TransactionContextRewriter}.
 */
class TransactionContextRewriterTest extends TraceRewriterTestTemplate {
    
    /**
     * Test case: Rewrite a trace containing all event types with a configuration that does not introduce any changes.
     */
    @Test
    void identityRewrite() {
        var fixture = this.createIdentityTraceFixture();
        
        var inputTrace = fixture.trace();
        var rewrittenTrace = new TransactionContextRewriter(fixture.deploymentModel()).rewriteTrace(inputTrace);

        assertEquals(fixture.rewrittenTrace(), rewrittenTrace);
    }
          
    /**
     * Test case: If a subordinate-propagation-capable transition is added within an explicitly started transaction, synthetic start and commit events are added.  
     */
    @Test
    void introduceSubordinatePropagationToExplicitTransaction() {
        final var traceId = 1234L;
        final var location = new ProcessLocation("test", 1234, 1);
        
        var originalUseCaseStartEvent = new UseCaseStartEvent(traceId, 0, location, "uc1");
        var originalTransactionStartEvent = new TransactionStartEvent(traceId, 50, location, "tx1");
        var originalServiceCandidateInvocationEvent = new ServiceCandidateInvocationEvent(traceId, 100, location, "sc1");
        var originalServiceCandidateEntryEvent = new ServiceCandidateEntryEvent(traceId, 110, location, "sc1", false, null);
        var originalServiceCandidateExitEvent = new ServiceCandidateExitEvent(traceId, 120, location, "sc1");
        var originalServiceCandidateReturnEvent = new ServiceCandidateReturnEvent(traceId, 130, location, "sc1");
        var originalTransactionCommitEvent = new TransactionCommitEvent(traceId, 150, location, "tx1");
        var originalUseCaseEndEvent = new UseCaseEndEvent(traceId, 200, location, "uc1");
        
        var inputTrace = EventTrace.of(
                originalUseCaseStartEvent,
                originalTransactionStartEvent,
                originalServiceCandidateInvocationEvent,
                originalServiceCandidateEntryEvent,
                originalServiceCandidateExitEvent,
                originalServiceCandidateReturnEvent,
                originalTransactionCommitEvent,
                originalUseCaseEndEvent
                );
        
        var component1 = new Component("c1");
        var component2 = new Component("c2");            
        
        var useCase = new UseCase("uc1");
        
        var candidate = new ServiceCandidate("sc1", TransactionBehavior.REQUIRED); 
        
        var originalDeploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component1)
                .assignServiceCandidate(candidate, component2)
                .addLocalConnection(component1, component2)
                .build();
        
        var modifiedDeploymentModel = originalDeploymentModel.applyModifications()
                .addSymmetricRemoteConnection(component1, component2, 0, TransactionPropagation.SUBORDINATE)
                .build();
                
        var rewriter = new TransactionContextRewriter(modifiedDeploymentModel);
        var rewrittenTrace = rewriter.rewriteTrace(inputTrace);

        var syntheticLocation = new SyntheticLocation(0);
        
        var rewrittenUseCaseStartEvent = new UseCaseStartEvent(traceId, 0, location, "uc1"); 
        var rewrittenTransactionStartEvent = new TransactionStartEvent(traceId, 50, location, "tx1"); 
        var rewrittenServiceCandidateInvocationEvent = new ServiceCandidateInvocationEvent(traceId, 100, location, "sc1"); 
        var rewrittenServiceCandidateEntryEvent = new ServiceCandidateEntryEvent(traceId, 110, syntheticLocation, "sc1", true, "synthetic-0"); 
        var rewrittenServiceCandidateExitEvent = new ServiceCandidateExitEvent(traceId, 120, syntheticLocation, "sc1"); 
        var rewrittenServiceCandidateReturnEvent = new ServiceCandidateReturnEvent(traceId, 130, location, "sc1"); 
        var rewrittenTransactionCommitEvent = new TransactionCommitEvent(traceId, 150, location, "tx1"); 
        var rewrittenUseCaseEndEvent = new UseCaseEndEvent(traceId, 200, location, "uc1"); 
        
        var expectedEvents = List.<MonitoringEvent> of(
                rewrittenUseCaseStartEvent,
                rewrittenTransactionStartEvent,
                rewrittenServiceCandidateInvocationEvent,
                rewrittenServiceCandidateEntryEvent,
                rewrittenServiceCandidateExitEvent,
                rewrittenServiceCandidateReturnEvent,
                rewrittenTransactionCommitEvent,
                rewrittenUseCaseEndEvent
                );
        
        var expectedCorrespondence = Map.<MonitoringEvent, MonitoringEvent> of(
                rewrittenUseCaseStartEvent, originalUseCaseStartEvent, //
                rewrittenTransactionStartEvent, originalTransactionStartEvent, //
                rewrittenServiceCandidateInvocationEvent, originalServiceCandidateInvocationEvent, //
                rewrittenServiceCandidateEntryEvent, originalServiceCandidateEntryEvent, //
                rewrittenServiceCandidateExitEvent, originalServiceCandidateExitEvent, //
                rewrittenServiceCandidateReturnEvent, originalServiceCandidateReturnEvent, //
                rewrittenTransactionCommitEvent, originalTransactionCommitEvent, //
                rewrittenUseCaseEndEvent, originalUseCaseEndEvent
                );                
        
        assertEquals(new RewrittenEventTrace(expectedEvents, expectedCorrespondence), rewrittenTrace);
    }
    
    /**
     * Test case: If a subordinate-propagation-capable transition is added within an implicitly started transaction, synthetic start and commit events are added.
     */
    @Test
    void introduceSubordinatePropagationToImplicitTransaction() {
        final var traceId = 1234L;
        final var location = new ProcessLocation("test", 1234, 1);
        
        var originalUseCaseStartEvent = new UseCaseStartEvent(traceId, 0, location, "uc1");
        var originalServiceCandidateInvocationEvent1 = new ServiceCandidateInvocationEvent(traceId, 100, location, "sc1");
        var originalServiceCandidateEntryEvent1 = new ServiceCandidateEntryEvent(traceId, 110, location, "sc1", true, "tx1");
        var originalServiceCandidateInvocationEvent2 = new ServiceCandidateInvocationEvent(traceId, 120, location, "sc2");
        var originalServiceCandidateEntryEvent2 = new ServiceCandidateEntryEvent(traceId, 130, location, "sc2", false, null);
        var originalServiceCandidateExitEvent2 = new ServiceCandidateExitEvent(traceId, 140, location, "sc2");
        var originalServiceCandidateReturnEvent2 = new ServiceCandidateReturnEvent(traceId, 150, location, "sc2");
        var originalServiceCandidateExitEvent1 = new ServiceCandidateExitEvent(traceId, 200, location, "sc2");
        var originalServiceCandidateReturnEvent1 = new ServiceCandidateReturnEvent(traceId, 210, location, "sc2");
        var originalUseCaseEndEvent = new UseCaseEndEvent(traceId, 300, location, "uc1");
        
        var inputTrace = EventTrace.of(
                originalUseCaseStartEvent,
                originalServiceCandidateInvocationEvent1,
                originalServiceCandidateEntryEvent1,
                originalServiceCandidateInvocationEvent2,
                originalServiceCandidateEntryEvent2,
                originalServiceCandidateExitEvent2,
                originalServiceCandidateReturnEvent2,
                originalServiceCandidateExitEvent1,
                originalServiceCandidateReturnEvent1,
                originalUseCaseEndEvent
                );
        
        var component1 = new Component("c1");
        var component2 = new Component("c2");        
        
        var useCase = new UseCase("uc1");
        
        var candidate1 = new ServiceCandidate("sc1", TransactionBehavior.REQUIRED); 
        var candidate2 = new ServiceCandidate("sc2", TransactionBehavior.REQUIRED);

        var originalDeploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component1)
                .assignServiceCandidate(candidate1, component2)
                .assignServiceCandidate(candidate2, component1)
                .addLocalConnection(component1, component2)
                .build();
        
        var modifiedDeploymentModel = originalDeploymentModel.applyModifications()
                .addSymmetricRemoteConnection(component1, component2, 0, TransactionPropagation.SUBORDINATE)
                .build();
                
        var rewriter = new TransactionContextRewriter(modifiedDeploymentModel);
        var rewrittenTrace = rewriter.rewriteTrace(inputTrace);

        var syntheticLocation1 = new SyntheticLocation(0);
        var syntheticLocation2 = new SyntheticLocation(1);
        
        var rewrittenUseCaseStartEvent = new UseCaseStartEvent(traceId, 0, location, "uc1");
        var rewrittenServiceCandidateInvocationEvent1 = new ServiceCandidateInvocationEvent(traceId, 100, location, "sc1"); 
        var rewrittenServiceCandidateEntryEvent1 = new ServiceCandidateEntryEvent(traceId, 110, syntheticLocation1, "sc1", true, "tx1");
        var rewrittenServiceCandidateInvocationEvent2 = new ServiceCandidateInvocationEvent(traceId, 120, syntheticLocation1, "sc2");
        var rewrittenServiceCandidateEntryEvent2 = new ServiceCandidateEntryEvent(traceId, 130, syntheticLocation2, "sc2", true, "synthetic-0");
        var rewrittenServiceCandidateExitEvent2 = new ServiceCandidateExitEvent(traceId, 140, syntheticLocation2, "sc2");
        var rewrittenServiceCandidateReturnEvent2 = new ServiceCandidateReturnEvent(traceId, 150, syntheticLocation1, "sc2");
        var rewrittenServiceCandidateExitEvent1 = new ServiceCandidateExitEvent(traceId, 200, syntheticLocation1, "sc2");
        var rewrittenServiceCandidateReturnEvent1 = new ServiceCandidateReturnEvent(traceId, 210, location, "sc2");
        var rewrittenUseCaseEndEvent = new UseCaseEndEvent(traceId, 300, location, "uc1");
        
        var expectedEvents = List.<MonitoringEvent> of(
                rewrittenUseCaseStartEvent,
                rewrittenServiceCandidateInvocationEvent1,
                rewrittenServiceCandidateEntryEvent1,
                rewrittenServiceCandidateInvocationEvent2,
                rewrittenServiceCandidateEntryEvent2,
                rewrittenServiceCandidateExitEvent2,
                rewrittenServiceCandidateReturnEvent2,
                rewrittenServiceCandidateExitEvent1,
                rewrittenServiceCandidateReturnEvent1,
                rewrittenUseCaseEndEvent
                );
        
        var expectedCorrespondence = new HashMap<MonitoringEvent, MonitoringEvent>();
        expectedCorrespondence.put(rewrittenUseCaseStartEvent, originalUseCaseStartEvent);
        expectedCorrespondence.put(rewrittenServiceCandidateInvocationEvent1, originalServiceCandidateInvocationEvent1);
        expectedCorrespondence.put(rewrittenServiceCandidateEntryEvent1, originalServiceCandidateEntryEvent1);
        expectedCorrespondence.put(rewrittenServiceCandidateInvocationEvent2, originalServiceCandidateInvocationEvent2);
        expectedCorrespondence.put(rewrittenServiceCandidateEntryEvent2, originalServiceCandidateEntryEvent2);
        expectedCorrespondence.put(rewrittenServiceCandidateExitEvent2, originalServiceCandidateExitEvent2);
        expectedCorrespondence.put(rewrittenServiceCandidateReturnEvent2, originalServiceCandidateReturnEvent2);
        expectedCorrespondence.put(rewrittenServiceCandidateExitEvent1, originalServiceCandidateExitEvent1);
        expectedCorrespondence.put(rewrittenServiceCandidateReturnEvent1, originalServiceCandidateReturnEvent1);
        expectedCorrespondence.put(rewrittenUseCaseEndEvent, originalUseCaseEndEvent);
        
        assertEquals(new RewrittenEventTrace(expectedEvents, expectedCorrespondence), rewrittenTrace);
    }
    
    /**
     * Test case: If a subordinate-propagation-capable transition is replaced by a local transition, the transaction is removed from the entry event and the
     * transaction ID is adjusted in implicit abort events.
     */
    @Test
    void internalizeSubordinatePropagationFromExplicitTransaction() {
        final var traceId = 1234L;
        final var location1 = new ProcessLocation("test", 1234, 1);
        final var location2 = new ProcessLocation("test", 5678, 1);
        
        var originalUseCaseStartEvent = new UseCaseStartEvent(traceId, 0, location1, "uc1");
        var originalTransactionStartEvent = new TransactionStartEvent(traceId, 50, location1, "tx1");
        var originalServiceCandidateInvocationEvent = new ServiceCandidateInvocationEvent(traceId, 100, location1, "sc1");
        var originalServiceCandidateEntryEvent = new ServiceCandidateEntryEvent(traceId, 110, location2, "sc1", true, "tx2");
        var originalTransactionAbortEvent = new ImplicitTransactionAbortEvent(traceId, 120, location2, "tx2", "cause");
        var originalServiceCandidateExitEvent = new ServiceCandidateExitEvent(traceId, 140, location2, "sc1");
        var originalServiceCandidateReturnEvent = new ServiceCandidateReturnEvent(traceId, 150, location1, "sc1");
        var originalTransactionCommitEvent = new TransactionCommitEvent(traceId, 160, location1, "tx1");
        var originalUseCaseEndEvent = new UseCaseEndEvent(traceId, 200, location1, "uc1");
        
        var inputTrace = EventTrace.of(
                originalUseCaseStartEvent,
                originalTransactionStartEvent,
                originalServiceCandidateInvocationEvent,
                originalServiceCandidateEntryEvent,
                originalTransactionAbortEvent,
                originalServiceCandidateExitEvent,
                originalServiceCandidateReturnEvent,
                originalTransactionCommitEvent,
                originalUseCaseEndEvent
                );
        
        var component1 = new Component("c1");
        var component2 = new Component("c2");
        
        var useCase = new UseCase("uc1");
        
        var candidate = new ServiceCandidate("sc1", TransactionBehavior.REQUIRED);

        var originalDeploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component1)
                .assignServiceCandidate(candidate, component2)
                .addSymmetricRemoteConnection(component1, component2, 10, TransactionPropagation.SUBORDINATE)
                .build();
        
        var modifiedDeploymentModel = originalDeploymentModel.applyModifications()
                .addLocalConnection(component1, component2)
                .build();
                
        var rewriter = new TransactionContextRewriter(modifiedDeploymentModel);
        var rewrittenTrace = rewriter.rewriteTrace(inputTrace);

        var rewrittenUseCaseStartEvent = new UseCaseStartEvent(traceId, 0, location1, "uc1");
        var rewrittenTransactionStartEvent = new TransactionStartEvent(traceId, 50, location1, "tx1");
        var rewrittenServiceCandidateInvocationEvent = new ServiceCandidateInvocationEvent(traceId, 100, location1, "sc1");
        var rewrittenServiceCandidateEntryEvent = new ServiceCandidateEntryEvent(traceId, 110, location1, "sc1", false, null);
        var rewrittenTransactionAbortEvent = new ImplicitTransactionAbortEvent(traceId, 120, location1, "tx1", "cause");
        var rewrittenServiceCandidateExitEvent = new ServiceCandidateExitEvent(traceId, 140, location1, "sc1");
        var rewrittenServiceCandidateReturnEvent = new ServiceCandidateReturnEvent(traceId, 150, location1, "sc1");
        var rewrittenTransactionCommitEvent = new TransactionCommitEvent(traceId, 160, location1, "tx1");
        var rewrittenUseCaseEndEvent = new UseCaseEndEvent(traceId, 200, location1, "uc1");
        
        var expectedEvents = List.<MonitoringEvent> of(
                rewrittenUseCaseStartEvent,
                rewrittenTransactionStartEvent,
                rewrittenServiceCandidateInvocationEvent,
                rewrittenServiceCandidateEntryEvent,
                rewrittenTransactionAbortEvent,
                rewrittenServiceCandidateExitEvent,
                rewrittenServiceCandidateReturnEvent,
                rewrittenTransactionCommitEvent,
                rewrittenUseCaseEndEvent                
                );
        
        var expectedCorrespondence = new HashMap<MonitoringEvent, MonitoringEvent>();
        expectedCorrespondence.put(rewrittenUseCaseStartEvent, originalUseCaseStartEvent);
        expectedCorrespondence.put(rewrittenTransactionStartEvent, originalTransactionStartEvent);
        expectedCorrespondence.put(rewrittenServiceCandidateInvocationEvent, originalServiceCandidateInvocationEvent);
        expectedCorrespondence.put(rewrittenServiceCandidateEntryEvent, originalServiceCandidateEntryEvent);
        expectedCorrespondence.put(rewrittenTransactionAbortEvent, originalTransactionAbortEvent);
        expectedCorrespondence.put(rewrittenServiceCandidateExitEvent, originalServiceCandidateExitEvent);
        expectedCorrespondence.put(rewrittenServiceCandidateReturnEvent, originalServiceCandidateReturnEvent);
        expectedCorrespondence.put(rewrittenTransactionCommitEvent, originalTransactionCommitEvent);
        expectedCorrespondence.put(rewrittenUseCaseEndEvent, originalUseCaseEndEvent);
        
        assertEquals(new RewrittenEventTrace(expectedEvents, expectedCorrespondence), rewrittenTrace);
    }
    
}
