package gutta.prediction.rewriting;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnections;
import gutta.prediction.domain.LocalComponentConnection;
import gutta.prediction.domain.RemoteComponentConnection;
import gutta.prediction.domain.RemoteComponentConnection.TransactionPropagation;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ProcessLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionAbortEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TraceRewriterTest {

    /**
     * Test case: Rewrite a trace containing all event types with a configuration that does not introduce any changes.
     */
    @Test
    void identityRewrite() {
        final var traceId = 1234L;
        final var location = new ProcessLocation("test", 1234, 1);

        var inputTrace = Arrays.<MonitoringEvent>asList(
                new UseCaseStartEvent(traceId, 100, location, "uc1"),
                new TransactionStartEvent(traceId, 200, location, "tx1"),
                // Same timestamp for invocation and entry as to avoid latency adjustment
                new ServiceCandidateInvocationEvent(traceId, 300, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 300, location, "sc1"),
                // Again, same timestamp for exit and return
                new ServiceCandidateExitEvent(traceId, 400, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 400, location, "sc1"),
                new TransactionCommitEvent(traceId, 500, location, "tx1"),
                new TransactionStartEvent(traceId, 600, location, "tx2"),
                new EntityReadEvent(traceId, 700, location, "et1", "id1"),
                new EntityWriteEvent(traceId, 800, location, "et1", "id1"),
                new TransactionAbortEvent(traceId, 900, location, "tx2", "NullPointerException"),
                new UseCaseEndEvent(traceId, 1000, location, "uc1")
                );

        var component = new Component("test");
        var useCaseAllocation = Collections.singletonMap("uc1", component);
        var methodAllocation = Collections.singletonMap("sc1", component);

        var rewrittenTrace = new TraceRewriter().rewriteTrace(inputTrace, useCaseAllocation, methodAllocation, new ComponentConnections());

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

        var useCaseAllocation = Collections.singletonMap("uc1", component1);
        var methodAllocation = Collections.singletonMap("sc1", component2);

        var rewrittenTrace = new TraceRewriter().rewriteTrace(inputTrace, useCaseAllocation, methodAllocation, new ComponentConnections(connectionC1C2));

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
     * Test case: For non-local connections, a location change is introduced.
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

        var useCaseAllocation = Collections.singletonMap("uc1", component1);
        var methodAllocation = Collections.singletonMap("sc1", component2);

        var artificialLocation = new SyntheticLocation();
        
        var rewrittenTrace = new TraceRewriterWithGivenArtificialLocations(artificialLocation).rewriteTrace(inputTrace, useCaseAllocation, methodAllocation, new ComponentConnections(connectionC1C2));

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
    
    private static class TraceRewriterWithGivenArtificialLocations extends TraceRewriter {
        
        private final Iterator<SyntheticLocation> locations;
        
        public TraceRewriterWithGivenArtificialLocations(SyntheticLocation... locations) {
            this.locations = Arrays.asList(locations).iterator();
        }
        
        @Override
        SyntheticLocation createArtificialLocation() {
            if (this.locations.hasNext()) {
                return this.locations.next();
            } else {
                throw new IllegalStateException();
            }
        }
        
    }

}
