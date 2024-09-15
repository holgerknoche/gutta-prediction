package gutta.prediction.analysis;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnections;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ProcessLocation;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.TransactionStartEvent.Demarcation;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

class EntityAccessConflictAnalysisTest {

    @Test
    void localTraceWithNoConflict() {
        final var traceId = 1234L;
        final var location = new ProcessLocation("test", 1234, 1);

        var inputEvents = Arrays.<MonitoringEvent> asList(
                new UseCaseStartEvent(traceId, 100, location, "uc1"),
                new TransactionStartEvent(traceId, 200, location, "tx1", Demarcation.EXPLICIT),
                new EntityReadEvent(traceId, 300, location, "et1", "id1"),
                new EntityWriteEvent(traceId, 400, location, "et1", "id1"),
                new EntityReadEvent(traceId, 500, location, "et1", "id1"),
                new TransactionCommitEvent(traceId, 600, location, "tx1"),
                new UseCaseEndEvent(traceId, 700, location, "uc1")
                );
        
        var useCaseAllocation = Collections.<String, Component> emptyMap();
        var methodAllocation = Collections.<String, Component> emptyMap();
        var connections = new ComponentConnections();
        
        new EntityAccessConflictAnalysis().performAnalysis(inputEvents, Collections.emptyList(), useCaseAllocation, methodAllocation, connections);
        
    }
    
}
