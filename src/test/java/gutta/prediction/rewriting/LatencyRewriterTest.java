package gutta.prediction.rewriting;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnections;
import gutta.prediction.domain.LocalComponentConnection;
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
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for the class {@link LatencyRewriter}.
 */
class LatencyRewriterTest extends TraceRewriterTestTemplate {

    /**
     * Test case: Rewrite a trace containing all event types with a configuration that does not introduce any changes.
     */
    @Test
    void identityRewrite() {
        var fixture = this.createIdentityTraceFixture();
        
        var inputTrace = fixture.trace();
        var rewrittenTrace = new LatencyRewriter(fixture.serviceCandidates(), fixture.useCaseAllocation(), fixture.candidateAllocation(), new ComponentConnections()).rewriteTrace(inputTrace);

        assertEquals(inputTrace, rewrittenTrace);
    }

    /**
     * Test case: A local transition removes existing latency (if any) and does not introduce a location change.
     */
    @Test
    void localTransition() {
        final var traceId = 1234L;
        final var location = new ProcessLocation("test", 1234, 1);

        var inputTrace = Arrays.<MonitoringEvent>asList(
                new UseCaseStartEvent(traceId, 100, location, "uc1"),
                new ServiceCandidateInvocationEvent(traceId, 200, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 210, location, "sc1"),
                new ServiceCandidateExitEvent(traceId, 400, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 410, location, "sc1"),
                new UseCaseEndEvent(traceId, 500, location, "uc1")
                );
        
        var component1 = new Component("comp1");
        var component2 = new Component("comp2");

        var connectionC1C2 = new LocalComponentConnection(component1, component2, true);

        var candidate = new ServiceCandidate("sc1", TransactionBehavior.SUPPORTED);
        
        var useCaseAllocation = Collections.singletonMap("uc1", component1);
        var candidateAllocation = Collections.singletonMap(candidate, component2);

        var rewrittenTrace = new LatencyRewriter(Collections.singletonList(candidate), useCaseAllocation, candidateAllocation, new ComponentConnections(connectionC1C2)).rewriteTrace(inputTrace);

        var expectedTrace = Arrays.<MonitoringEvent>asList(
                new UseCaseStartEvent(traceId, 100, location, "uc1"),
                new ServiceCandidateInvocationEvent(traceId, 200, location, "sc1"),
                // Remove latency from the input trace
                new ServiceCandidateEntryEvent(traceId, 200, location, "sc1"),
                new ServiceCandidateExitEvent(traceId, 390, location, "sc1"),
                // Again, latency is removed
                new ServiceCandidateReturnEvent(traceId, 390, location, "sc1"),
                new UseCaseEndEvent(traceId, 480, location, "uc1")
                );

        assertEquals(expectedTrace, rewrittenTrace);
    }
    
    /**
     * Test case: For remote connections, a location change is introduced and latency is adjusted.
     */
    @Test
    void locationChange() {
        final var traceId = 1234L;
        final var location = new ProcessLocation("test", 1234, 1);

        var inputTrace = Arrays.<MonitoringEvent>asList(
                new UseCaseStartEvent(traceId, 100, location, "uc1"),
                new ServiceCandidateInvocationEvent(traceId, 200, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 210, location, "sc1"),
                new ServiceCandidateExitEvent(traceId, 400, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 410, location, "sc1"),
                new UseCaseEndEvent(traceId, 500, location, "uc1")
                );

        var component1 = new Component("comp1");
        var component2 = new Component("comp2");

        var connectionC1C2 = new RemoteComponentConnection(component1, component2, true, 50, TransactionPropagation.NONE, true);
        
        var candidate = new ServiceCandidate("sc1", TransactionBehavior.SUPPORTED);

        var useCaseAllocation = Collections.singletonMap("uc1", component1);
        var candidateAllocation = Collections.singletonMap(candidate, component2);

        var artificialLocation = new SyntheticLocation(0);
        
        var rewrittenTrace = new LatencyRewriter(Collections.singletonList(candidate), useCaseAllocation, candidateAllocation, new ComponentConnections(connectionC1C2)).rewriteTrace(inputTrace);

        var expectedTrace = Arrays.<MonitoringEvent>asList(
                new UseCaseStartEvent(traceId, 100, location, "uc1"),
                new ServiceCandidateInvocationEvent(traceId, 200, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 250, artificialLocation, "sc1"),
                new ServiceCandidateExitEvent(traceId, 440, artificialLocation, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 490, location, "sc1"),
                new UseCaseEndEvent(traceId, 580, location, "uc1")
                );

        assertEquals(expectedTrace, rewrittenTrace);
    }
    

}
