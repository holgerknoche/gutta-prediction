package gutta.prediction.rewriting;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnections;
import gutta.prediction.domain.RemoteComponentConnection;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.TransactionPropagation;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ProcessLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.TransactionStartEvent.Demarcation;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        var rewrittenTrace = new TransactionContextRewriter(fixture.serviceCandidates(), fixture.useCaseAllocation(), fixture.candidateAllocation(), new ComponentConnections()).rewriteTrace(inputTrace);

        assertEquals(inputTrace, rewrittenTrace);
    }
    
    /**
     * Test case: An error occurs when a transaction is already active when an explicitly demarcated transaction is started. 
     */
    @Test
    void activeTransactionWhenExplicitTransactionIsStarted() {        
        final var traceId = 1234L;
        final var location = new ProcessLocation("test", 1234, 1);

        // This event must have explicit transaction demarcation to trigger the error
        final var offendingEvent = new TransactionStartEvent(traceId, 100, location, "tx2", Demarcation.EXPLICIT); 
        
        final var inputEvents = Arrays.<MonitoringEvent> asList(
                new UseCaseStartEvent(traceId, 0, location, "uc1"),
                new TransactionStartEvent(traceId, 50, location, "tx1", Demarcation.EXPLICIT),
                offendingEvent,
                new UseCaseEndEvent(traceId, 200, location, "uc1")
                );
        
        var component = new Component("c1");                
        var useCaseAllocation = Collections.singletonMap("uc1", component);
        
        var candidate = new ServiceCandidate("sc1", TransactionBehavior.SUPPORTED);
        var candidateAllocation = Map.of(candidate, component);
       
        var rewriter = new TransactionContextRewriter(Collections.singletonList(candidate), useCaseAllocation, candidateAllocation, new ComponentConnections()); 
        var exception = assertThrows(TraceRewriteException.class, () -> rewriter.rewriteTrace(inputEvents));
        
        // Make sure that the exception has the expected message and occurs at the expected event
        assertEquals(offendingEvent, exception.offendingEvent());
        assertTrue(exception.getMessage().contains("A transaction was active"));
    }   
    
    /**
     * Test case: If a subordinate-propagation-capable transition is added within an explicitly started transaction, synthetic start and commit events are added.  
     */
    @Test
    void introduceSubordinatePropagationToExplicitTransaction() {
        final var traceId = 1234L;
        final var location = new ProcessLocation("test", 1234, 1);
        
        final var inputEvents = Arrays.<MonitoringEvent> asList(
                new UseCaseStartEvent(traceId, 0, location, "uc1"),
                new TransactionStartEvent(traceId, 50, location, "tx1", Demarcation.EXPLICIT),
                new ServiceCandidateInvocationEvent(traceId, 100, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 110, location, "sc1"),
                new ServiceCandidateExitEvent(traceId, 120, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 130, location, "sc1"),
                new TransactionCommitEvent(traceId, 150, location, "tx1"),
                new UseCaseEndEvent(traceId, 200, location, "uc1")
                );
        
        var component1 = new Component("c1");
        var component2 = new Component("c2");
        var useCaseAllocation = Collections.singletonMap("uc1", component1);
        
        var candidate = new ServiceCandidate("sc1", TransactionBehavior.REQUIRED); 
        var candidateAllocation = Map.of(candidate, component2);
        
        var connectionC1C2 = new RemoteComponentConnection(component1, component2, true, 0, TransactionPropagation.SUBORDINATE, true);
        
        var rewriter = new TransactionContextRewriter(Collections.singletonList(candidate), useCaseAllocation, candidateAllocation, new ComponentConnections(connectionC1C2));
        var rewrittenTrace = rewriter.rewriteTrace(inputEvents);

        var syntheticLocation = new SyntheticLocation(0);
        var expectedTrace = Arrays.<MonitoringEvent> asList(
                new UseCaseStartEvent(traceId, 0, location, "uc1"),
                new TransactionStartEvent(traceId, 50, location, "tx1", Demarcation.EXPLICIT),
                new ServiceCandidateInvocationEvent(traceId, 100, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 110, syntheticLocation, "sc1"),
                new TransactionStartEvent(traceId, 110, syntheticLocation, "synthetic-0", Demarcation.IMPLICIT),
                new TransactionCommitEvent(traceId, 120, syntheticLocation, "synthetic-0"),
                new ServiceCandidateExitEvent(traceId, 120, syntheticLocation, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 130, location, "sc1"),
                new TransactionCommitEvent(traceId, 150, location, "tx1"),
                new UseCaseEndEvent(traceId, 200, location, "uc1")
                );
        
        assertEquals(expectedTrace, rewrittenTrace);
    }
    
    /**
     * Test case: If a subordinate-propagation-capable transition is added within an implicitly started transaction, synthetic start and commit events are added.
     */
    @Test
    void introduceSubordinatePropagationToImplicitTransaction() {
        final var traceId = 1234L;
        final var location = new ProcessLocation("test", 1234, 1);
        
        final var inputEvents = Arrays.<MonitoringEvent> asList(
                new UseCaseStartEvent(traceId, 0, location, "uc1"),
                new ServiceCandidateInvocationEvent(traceId, 100, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 110, location, "sc1"),
                new ServiceCandidateInvocationEvent(traceId, 120, location, "sc2"),
                new ServiceCandidateEntryEvent(traceId, 130, location, "sc2"),
                new ServiceCandidateExitEvent(traceId, 140, location, "sc2"),
                new ServiceCandidateReturnEvent(traceId, 150, location, "sc2"),
                new ServiceCandidateExitEvent(traceId, 200, location, "sc2"),
                new ServiceCandidateReturnEvent(traceId, 210, location, "sc2"),
                new UseCaseEndEvent(traceId, 300, location, "uc1")
                );
        
        var component1 = new Component("c1");
        var component2 = new Component("c2");        
        var useCaseAllocation = Collections.singletonMap("uc1", component1);
        
        var candidate1 = new ServiceCandidate("sc1", TransactionBehavior.REQUIRED); 
        var candidate2 = new ServiceCandidate("sc2", TransactionBehavior.REQUIRED);
        
        var candidateAllocation = Map.of(candidate1, component2, candidate2, component1);        
        var connectionC1C2 = new RemoteComponentConnection(component1, component2, true, 0, TransactionPropagation.SUBORDINATE, true);
        
        var rewriter = new TransactionContextRewriter(List.of(candidate1, candidate2), useCaseAllocation, candidateAllocation, new ComponentConnections(connectionC1C2));
        var rewrittenTrace = rewriter.rewriteTrace(inputEvents);

        var syntheticLocation1 = new SyntheticLocation(0);
        var syntheticLocation2 = new SyntheticLocation(1);
        
        var expectedTrace = Arrays.<MonitoringEvent> asList(
                new UseCaseStartEvent(traceId, 0, location, "uc1"),
                new ServiceCandidateInvocationEvent(traceId, 100, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 110, syntheticLocation1, "sc1"),
                new TransactionStartEvent(traceId, 110, syntheticLocation1, "synthetic-0", Demarcation.IMPLICIT),
                new ServiceCandidateInvocationEvent(traceId, 120, syntheticLocation1, "sc2"),
                new ServiceCandidateEntryEvent(traceId, 130, syntheticLocation2, "sc2"),
                new TransactionStartEvent(traceId, 130, syntheticLocation2, "synthetic-1", Demarcation.IMPLICIT),
                new TransactionCommitEvent(traceId, 140, syntheticLocation2, "synthetic-1"),
                new ServiceCandidateExitEvent(traceId, 140, syntheticLocation2, "sc2"),
                new ServiceCandidateReturnEvent(traceId, 150, syntheticLocation1, "sc2"),
                new TransactionCommitEvent(traceId, 200, syntheticLocation1, "synthetic-0"),
                new ServiceCandidateExitEvent(traceId, 200, syntheticLocation1, "sc2"),
                new ServiceCandidateReturnEvent(traceId, 210, location, "sc2"),
                new UseCaseEndEvent(traceId, 300, location, "uc1")
                );
        
        assertEquals(expectedTrace, rewrittenTrace);
    }
    
}
