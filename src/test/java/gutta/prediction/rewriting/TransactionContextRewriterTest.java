package gutta.prediction.rewriting;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnections;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ProcessLocation;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.TransactionStartEvent.Demarcation;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

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
        var candidateAllocation = Collections.singletonMap(candidate, component);
       
        var rewriter = new TransactionContextRewriter(Collections.singletonList(candidate), useCaseAllocation, candidateAllocation, new ComponentConnections()); 
        var exception = assertThrows(TraceRewriteException.class, () -> rewriter.rewriteTrace(inputEvents));
        
        // Make sure that the exception has the expected message and occurs at the expected event
        assertEquals(offendingEvent, exception.offendingEvent());
        assertTrue(exception.getMessage().contains("Found an active transaction"));
    }

}
