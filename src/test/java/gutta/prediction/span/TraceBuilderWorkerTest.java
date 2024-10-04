package gutta.prediction.span;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.TransactionPropagation;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ProcessLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for the class {@link TraceBuilderWorker}.
 */
class TraceBuilderWorkerTest {
    
    /**
     * Test case: A "plain" event trace, i.e., a trace without location changes, is transformed into a single span. Latency overlays are created, if any.
     */
    @Test
    void plainEventTrace() {
        var location = new ProcessLocation("test", 1234, 0);
        var traceId = 1234;
        
        var eventTrace = EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location, "uc"),
                new ServiceCandidateInvocationEvent(traceId, 200, location, "sc"),
                new ServiceCandidateEntryEvent(traceId, 250, location, "sc"),
                new ServiceCandidateExitEvent(traceId, 350, location, "sc"),
                new ServiceCandidateReturnEvent(traceId, 400, location, "sc"),
                new UseCaseEndEvent(traceId, 1000, location, "uc")
                );
        
        var useCase = new UseCase("uc");        
        var serviceCandidate = new ServiceCandidate("sc", TransactionBehavior.SUPPORTED);
        var component = new Component("component");
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component)
                .assignServiceCandidate(serviceCandidate, component)
                .build();
        
        var worker = new TraceBuilderWorker();
        var spanTrace = worker.buildTrace(eventTrace, deploymentModel, Set.of());
        
        var expectedRootSpan = new Span("uc", 100, 1000, null, List.of(), List.of(new LatencyOverlay(200, 250), new LatencyOverlay(350, 400)));
        var expectedTrace = new Trace(1234, "uc", expectedRootSpan);
        
        assertEquals(expectedTrace, spanTrace);
    }
    
    /**
     * Test case: A event trace that spans multiple locations is transformed into an appropriate span structure. Latency overlays are created at the appropriate spans.
     */
    @Test
    void traceWithMultipleLocations() {
        var location1 = new ProcessLocation("test", 1234, 0);
        var location2 = new ProcessLocation("test", 1234, 1);
        var traceId = 1234;
        
        var eventTrace = EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location1, "uc"),
                new ServiceCandidateInvocationEvent(traceId, 200, location1, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 250, location1, "sc1"),
                new ServiceCandidateInvocationEvent(traceId, 300, location1, "sc2"),
                new ServiceCandidateEntryEvent(traceId, 320, location2, "sc2"),
                new ServiceCandidateExitEvent(traceId, 380, location2, "sc2"),
                new ServiceCandidateReturnEvent(traceId, 400, location1, "sc2"),
                new ServiceCandidateExitEvent(traceId, 450, location1, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 500, location1, "sc1"),
                new UseCaseEndEvent(traceId, 1000, location1, "uc")
                );
        
        var useCase = new UseCase("uc");        
        var serviceCandidate1 = new ServiceCandidate("sc1", TransactionBehavior.SUPPORTED);
        var serviceCandidate2 = new ServiceCandidate("sc2", TransactionBehavior.SUPPORTED);
        
        var component1 = new Component("component1");
        var component2 = new Component("component2");
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component1)
                .assignServiceCandidate(serviceCandidate1, component1)
                .assignServiceCandidate(serviceCandidate2, component2)
                .addSymmetricRemoteConnection(component1, component2, 20, TransactionPropagation.NONE)
                .build();
        
        var worker = new TraceBuilderWorker();
        var spanTrace = worker.buildTrace(eventTrace, deploymentModel, Set.of());
        
        var expectedRootSpan = new Span("uc", 100, 1000, null, List.of(), List.of(new LatencyOverlay(200, 250), new LatencyOverlay(450, 500)));
        new Span("sc2", 320, 380, expectedRootSpan, List.of(), List.of(new LatencyOverlay(300, 320), new LatencyOverlay(380, 400)));
        
        var expectedTrace = new Trace(1234, "uc", expectedRootSpan);
        
        assertEquals(expectedTrace, spanTrace);
    }

}
