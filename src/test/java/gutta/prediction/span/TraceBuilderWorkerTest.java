package gutta.prediction.span;

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
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ObservedLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import gutta.prediction.span.EntityEvent.EntityAccessType;
import gutta.prediction.span.TransactionEvent.TransactionEventType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for the class {@link TraceBuilderWorker}.
 */
class TraceBuilderWorkerTest {
    
    /**
     * Test case: A "plain" event trace, i.e., a trace without location changes, is transformed into a single span. Overhead overlays are created, if any.
     */
    @Test
    void plainEventTrace() {
        var location = new ObservedLocation("test", 1234, 0);
        var traceId = 1234;
        
        var eventTrace = EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location, "uc"), //
                new ServiceCandidateInvocationEvent(traceId, 200, location, "sc"), //
                new ServiceCandidateEntryEvent(traceId, 250, location, "sc"), //
                new ServiceCandidateExitEvent(traceId, 350, location, "sc"), //
                new ServiceCandidateReturnEvent(traceId, 400, location, "sc"), //
                new UseCaseEndEvent(traceId, 1000, location, "uc") //
                );
        
        var useCase = new UseCase("uc");        
        var serviceCandidate = new ServiceCandidate("sc", TransactionBehavior.SUPPORTED);
        var component = new Component("component");
        
        var deploymentModel = new DeploymentModel.Builder() //
                .assignUseCaseToComponent(useCase, component) //
                .assignServiceCandidateToComponent(serviceCandidate, component) //
                .build();
        
        var worker = new TraceBuilderWorker();
        var spanTrace = worker.buildTrace(eventTrace, deploymentModel, Set.of());
        
        var expectedRootSpan = new Span("component", 100, 1000, null, List.of(), List.of(new OverheadOverlay(200, 250), new OverheadOverlay(350, 400)));
        var expectedTrace = new Trace(1234, "uc", expectedRootSpan);
        
        assertEquals(expectedTrace, spanTrace);
    }
    
    /**
     * Test case: A event trace that spans multiple locations is transformed into an appropriate span structure. Overhead overlays are created at the appropriate spans.
     */
    @Test
    void traceWithMultipleLocations() {
        var location1 = new ObservedLocation("test", 1234, 0);
        var location2 = new ObservedLocation("test", 1234, 1);
        var traceId = 1234;
        
        var eventTrace = EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location1, "uc"), //
                new ServiceCandidateInvocationEvent(traceId, 200, location1, "sc1"), //
                new ServiceCandidateEntryEvent(traceId, 250, location1, "sc1"), //
                new ServiceCandidateInvocationEvent(traceId, 300, location1, "sc2"), //
                new ServiceCandidateEntryEvent(traceId, 320, location2, "sc2"), //
                new ServiceCandidateExitEvent(traceId, 380, location2, "sc2"), //
                new ServiceCandidateReturnEvent(traceId, 400, location1, "sc2"), //
                new ServiceCandidateExitEvent(traceId, 450, location1, "sc1"), //
                new ServiceCandidateReturnEvent(traceId, 500, location1, "sc1"), //
                new UseCaseEndEvent(traceId, 1000, location1, "uc") //
                );
        
        var useCase = new UseCase("uc");        
        var serviceCandidate1 = new ServiceCandidate("sc1", TransactionBehavior.SUPPORTED);
        var serviceCandidate2 = new ServiceCandidate("sc2", TransactionBehavior.SUPPORTED);
        
        var component1 = new Component("component1");
        var component2 = new Component("component2");
        
        var deploymentModel = new DeploymentModel.Builder() //
                .assignUseCaseToComponent(useCase, component1) //
                .assignServiceCandidateToComponent(serviceCandidate1, component1) //
                .assignServiceCandidateToComponent(serviceCandidate2, component2) //
                .addSymmetricRemoteConnection(component1, component2, 20, TransactionPropagation.NONE) //
                .build();
        
        var worker = new TraceBuilderWorker();
        var spanTrace = worker.buildTrace(eventTrace, deploymentModel, Set.of());
        
        var expectedRootSpan = new Span("component1", 100, 1000, null, List.of(), List.of(new OverheadOverlay(200, 250), new OverheadOverlay(450, 500)));
        new Span("component2", 320, 380, expectedRootSpan, List.of(), List.of(new OverheadOverlay(300, 320), new OverheadOverlay(380, 400)));
        
        var expectedTrace = new Trace(1234, "uc", expectedRootSpan);
        
        assertEquals(expectedTrace, spanTrace);
    }

    // TODO Implicit transaction aborts
    
    /**
     * Test case: Appropriate transaction overlays are created for a trace with independent transactions.
     */
    @Test
    void transactionOverlaysForIndependentTransactions() {
        var location = new ObservedLocation("test", 1234, 0);
        var traceId = 1234;
        
        var entity = new Entity("et", "e");
        
        var dataStore = new DataStore("ds", ReadWriteConflictBehavior.STALE_READ);
        
        var eventTrace = EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location, "uc"), //
                new TransactionStartEvent(traceId, 200, location, "tx1"), //
                new EntityWriteEvent(traceId, 500, location, entity), //
                new TransactionCommitEvent(traceId, 800, location, "tx1"), //
                new UseCaseEndEvent(traceId, 1000, location, "uc") //
                );
        
        var useCase = new UseCase("uc");        
        var component = new Component("component");
        var entityType = new EntityType("et");
        
        var deploymentModel = new DeploymentModel.Builder() //
                .assignUseCaseToComponent(useCase, component) //
                .assignEntityTypeToDataStore(entityType, dataStore) //
                .build();
        
        var worker = new TraceBuilderWorker();
        var spanTrace = worker.buildTrace(eventTrace, deploymentModel, Set.of());
        
        var expectedRootSpanEvents = List.<SpanEvent>of(new TransactionEvent(200, TransactionEventType.START), new EntityEvent(500, EntityAccessType.WRITE, new Entity("et", "e")), new TransactionEvent(800, TransactionEventType.COMMIT));
        var expectedRootSpanOverlays = List.<SpanOverlay>of(new CleanTransactionOverlay(200, 500), new DirtyTransactionOverlay(500, 800));
        
        var expectedRootSpan = new Span("component", 100, 1000, null, expectedRootSpanEvents, expectedRootSpanOverlays);
        var expectedTrace = new Trace(1234, "uc", expectedRootSpan);                
        
        assertEquals(expectedTrace, spanTrace);
    }
    
    /**
     * Test case: Appropriate transaction overlays are created for a trace with subordinate transactions, especially a suspension overlay until the end of the top-level transaction.
     */
    @Test
    void transactionOverlaysForSubordinateTransactions() {
        var location1 = new ObservedLocation("test", 1234, 0);
        var location2 = new ObservedLocation("test", 1234, 1);
        var traceId = 1234;
        
        var entity = new Entity("et", "e");
        
        var dataStore = new DataStore("ds", ReadWriteConflictBehavior.STALE_READ);
        
        var eventTrace = EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location1, "uc"), //
                new TransactionStartEvent(traceId, 200, location1, "tx1"), //
                new ServiceCandidateInvocationEvent(traceId, 300, location1, "sc"), //
                new ServiceCandidateEntryEvent(traceId, 300, location2, "sc"), //
                new EntityWriteEvent(traceId, 500, location2, entity), //
                new ServiceCandidateExitEvent(traceId, 700, location2, "sc"), //
                new ServiceCandidateReturnEvent(traceId, 700, location1, "sc"), //                
                new TransactionCommitEvent(traceId, 800, location1, "tx1"), //
                new UseCaseEndEvent(traceId, 1000, location1, "uc") //
                );
        
        var useCase = new UseCase("uc");        
        var serviceCandidate = new ServiceCandidate("sc", TransactionBehavior.SUPPORTED);
        var component1 = new Component("component1");
        var component2 = new Component("component2");
        var entityType = new EntityType("et");
        
        var deploymentModel = new DeploymentModel.Builder() //
                .assignUseCaseToComponent(useCase, component1) //
                .assignServiceCandidateToComponent(serviceCandidate, component2) //
                .assignEntityTypeToDataStore(entityType, dataStore) //
                .addSymmetricRemoteConnection(component1, component2, 0, TransactionPropagation.SUBORDINATE) //
                .build();
        
        var worker = new TraceBuilderWorker();
        var spanTrace = worker.buildTrace(eventTrace, deploymentModel, Set.of());
        
        var expectedRootSpanEvents = List.<SpanEvent>of(new TransactionEvent(200, TransactionEventType.START), new TransactionEvent(800, TransactionEventType.COMMIT));
        var expectedRootSpanOverlays = List.<SpanOverlay>of(new CleanTransactionOverlay(200, 300), new SuspendedTransactionOverlay(300, 700, false), new CleanTransactionOverlay(700, 800));
        var expectedSubSpanEvents = List.<SpanEvent>of(new TransactionEvent(300, TransactionEventType.START), new EntityEvent(500, EntityAccessType.WRITE, entity), new TransactionEvent(800, TransactionEventType.COMMIT));
        var expectedSubSpanOverlays = List.<SpanOverlay>of(new CleanTransactionOverlay(300, 500), new DirtyTransactionOverlay(500, 700), new SuspendedTransactionOverlay(700, 800, true));
        
        var expectedRootSpan = new Span("component1", 100, 1000, null, expectedRootSpanEvents, expectedRootSpanOverlays);
        new Span("component2", 300, 700, expectedRootSpan, expectedSubSpanEvents, expectedSubSpanOverlays);
        
        var expectedTrace = new Trace(1234, "uc", expectedRootSpan);
                
        assertEquals(expectedTrace, spanTrace);
    }

}
