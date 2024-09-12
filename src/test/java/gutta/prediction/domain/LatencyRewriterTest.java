package gutta.prediction.domain;

import gutta.prediction.domain.ComponentConnectionProperties.ConnectionType;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

class LatencyRewriterTest {

    /**
     * Test case: Rewrite a trace containing all event types with a configuration that does not introduce any changes.
     */
    @Test
    void identityRewrite() {
        final var traceId = 1234L;
        final var location = new Location("test", 1234);

        var inputTrace = Arrays.<MonitoringEvent>asList(new UseCaseStartEvent(traceId, 100, location, "uc1"),
                new TransactionStartEvent(traceId, 200, location, "tx1"),
                // Same timestamp for invocation and entry as to avoid latency adjustment
                new ServiceCandidateInvocationEvent(traceId, 300, location, "sc1"), new ServiceCandidateEntryEvent(traceId, 300, location, "sc1"),
                // Again, same timestamp for exit and return
                new ServiceCandidateExitEvent(traceId, 400, location, "sc1"), new ServiceCandidateReturnEvent(traceId, 400, location, "sc1"),
                new TransactionCommitEvent(traceId, 500, location, "tx1"), new TransactionStartEvent(traceId, 600, location, "tx2"),
                new EntityReadEvent(traceId, 700, location, "et1", "id1"), new EntityWriteEvent(traceId, 800, location, "et1", "id1"),
                new TransactionAbortEvent(traceId, 900, location, "tx2", "NullPointerException"), new UseCaseEndEvent(traceId, 1000, location, "uc1"));

        var component = new Component("test");
        var useCaseAllocation = Collections.singletonMap("uc1", component);
        var methodAllocation = Collections.singletonMap("sc1", component);

        var rewrittenTrace = new LatencyTraceRewriter().rewriteTrace(inputTrace, useCaseAllocation, methodAllocation, new ComponentConnections());

        assertEquals(inputTrace, rewrittenTrace);
    }

    /**
     * Test case: Rewrite a trace with a configuration that introduces a single latency change.
     */
    @Test
    void singleLatencyChange() {
        final var traceId = 1234L;
        final var location = new Location("test", 1234);

        var inputTrace = Arrays.<MonitoringEvent>asList(new UseCaseStartEvent(traceId, 100, location, "uc1"),
                new ServiceCandidateInvocationEvent(traceId, 200, location, "sc1"), new ServiceCandidateEntryEvent(traceId, 210, location, "sc1"),
                new ServiceCandidateExitEvent(traceId, 400, location, "sc1"), new ServiceCandidateReturnEvent(traceId, 410, location, "sc1"),
                new UseCaseEndEvent(traceId, 500, location, "uc1"));

        var component1 = new Component("comp1");
        var component2 = new Component("comp2");

        var connectionC1C2 = new ComponentConnection(component1, component2, true, 50, ConnectionType.LOCAL, true);

        var useCaseAllocation = Collections.singletonMap("uc1", component1);
        var methodAllocation = Collections.singletonMap("sc1", component2);

        var rewrittenTrace = new LatencyTraceRewriter().rewriteTrace(inputTrace, useCaseAllocation, methodAllocation, new ComponentConnections(connectionC1C2));

        var expectedTrace = Arrays.<MonitoringEvent>asList(new UseCaseStartEvent(traceId, 100, location, "uc1"),
                new ServiceCandidateInvocationEvent(traceId, 200, location, "sc1"), new ServiceCandidateEntryEvent(traceId, 250, location, "sc1"),
                new ServiceCandidateExitEvent(traceId, 440, location, "sc1"), new ServiceCandidateReturnEvent(traceId, 490, location, "sc1"),
                new UseCaseEndEvent(traceId, 580, location, "uc1"));

        assertEquals(expectedTrace, rewrittenTrace);
    }

}
