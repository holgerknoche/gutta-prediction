package gutta.prediction.rewriting;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.TransactionPropagation;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ObservedLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.SyntheticLocation;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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
        var rewrittenTrace = new LatencyRewriter(fixture.deploymentModel()).rewriteTrace(inputTrace);

        assertEquals(fixture.rewrittenTrace(), rewrittenTrace);
    }

    /**
     * Test case: A local transition removes existing latency (if any) and does not introduce a location change.
     */
    @Test
    void localTransition() {
        final var traceId = 1234L;
        final var location = new ObservedLocation("test", 1234, 1);

        var originalUseCaseStartEvent = new UseCaseStartEvent(traceId, 100, location, "uc1");
        var originalServiceCandidateInvocationEvent = new ServiceCandidateInvocationEvent(traceId, 200, location, "sc1");
        var originalServiceCandidateEntryEvent = new ServiceCandidateEntryEvent(traceId, 210, location, "sc1", true, "tx1");
        var originalServiceCandidateExitEvent = new ServiceCandidateExitEvent(traceId, 400, location, "sc1");
        var originalServiceCandidateReturnEvent = new ServiceCandidateReturnEvent(traceId, 410, location, "sc1");
        var originalUseCaseEndEvent = new UseCaseEndEvent(traceId, 500, location, "uc1");
        
        var inputTrace = EventTrace.of(
                originalUseCaseStartEvent,
                originalServiceCandidateInvocationEvent,
                originalServiceCandidateEntryEvent,
                originalServiceCandidateExitEvent,
                originalServiceCandidateReturnEvent,
                originalUseCaseEndEvent
                );
        
        var component1 = new Component("comp1");
        var component2 = new Component("comp2");

        var useCase = new UseCase("uc1");
        var candidate = new ServiceCandidate("sc1", TransactionBehavior.SUPPORTED);
        
        var originalDeploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component1)
                .assignServiceCandidate(candidate, component2)
                .addSymmetricRemoteConnection(component1, component2, 10, TransactionPropagation.NONE)
                .build();
        
        var modifiedDeploymentModel = originalDeploymentModel.applyModifications()
                .addLocalConnection(component1, component2)
                .build();

        var rewriter = new LatencyRewriter(modifiedDeploymentModel);
        var rewrittenTrace = rewriter.rewriteTrace(inputTrace);

        var rewrittenUseCaseStartEvent = new UseCaseStartEvent(traceId, 100, location, "uc1");
        var rewrittenServiceCandidateInvocationEvent = new ServiceCandidateInvocationEvent(traceId, 200, location, "sc1");
        // Remove latency from the input trace
        var rewrittenServiceCandidateEntryEvent = new ServiceCandidateEntryEvent(traceId, 200, location, "sc1", true, "tx1");
        var rewrittenServiceCandidateExitEvent = new ServiceCandidateExitEvent(traceId, 390, location, "sc1");
        // Again, latency is removed
        var rewrittenServiceCandidateReturnEvent = new ServiceCandidateReturnEvent(traceId, 390, location, "sc1");
        var rewrittenUseCaseEndEvent = new UseCaseEndEvent(traceId, 480, location, "uc1");
        
        var expectedEvents = List.<MonitoringEvent> of(
                rewrittenUseCaseStartEvent,
                rewrittenServiceCandidateInvocationEvent,
                // Remove latency from the input trace
                rewrittenServiceCandidateEntryEvent,
                rewrittenServiceCandidateExitEvent,
                // Again, latency is removed
                rewrittenServiceCandidateReturnEvent,
                rewrittenUseCaseEndEvent
                );
        
        var expectedCorrespondence = Map.<MonitoringEvent, MonitoringEvent> of(
                rewrittenUseCaseStartEvent, originalUseCaseStartEvent, //
                rewrittenServiceCandidateInvocationEvent, originalServiceCandidateInvocationEvent, //
                rewrittenServiceCandidateEntryEvent, originalServiceCandidateEntryEvent, //
                rewrittenServiceCandidateExitEvent, originalServiceCandidateExitEvent, //
                rewrittenServiceCandidateReturnEvent, originalServiceCandidateReturnEvent, //
                rewrittenUseCaseEndEvent, originalUseCaseEndEvent
                );                

        assertEquals(new RewrittenEventTrace(expectedEvents, expectedCorrespondence), rewrittenTrace);
    }
    
    /**
     * Test case: For remote connections, a location change is introduced and latency is adjusted.
     */
    @Test
    void locationChange() {
        final var traceId = 1234L;
        final var location = new ObservedLocation("test", 1234, 1);

        var originalUseCaseStartEvent = new UseCaseStartEvent(traceId, 100, location, "uc1");
        var originalServiceCandidateInvocationEvent = new ServiceCandidateInvocationEvent(traceId, 200, location, "sc1");
        var originalServiceCandidateEntryEvent = new ServiceCandidateEntryEvent(traceId, 200, location, "sc1", true, "tx1");
        var originalServiceCandidateExitEvent = new ServiceCandidateExitEvent(traceId, 400, location, "sc1");
        var originalServiceCandidateReturnEvent = new ServiceCandidateReturnEvent(traceId, 400, location, "sc1");
        var originalUseCaseEndEvent = new UseCaseEndEvent(traceId, 500, location, "uc1");
        
        var inputTrace = EventTrace.of(
                originalUseCaseStartEvent,
                originalServiceCandidateInvocationEvent,
                originalServiceCandidateEntryEvent,
                originalServiceCandidateExitEvent,
                originalServiceCandidateReturnEvent,
                originalUseCaseEndEvent
                );

        var component1 = new Component("comp1");
        var component2 = new Component("comp2");

        var useCase = new UseCase("uc1");
        var candidate = new ServiceCandidate("sc1", TransactionBehavior.SUPPORTED);
        
        var originalDeploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component1)
                .assignServiceCandidate(candidate, component2)
                .addLocalConnection(component1, component2)
                .build();
        
        var modifiedDeploymentModel = originalDeploymentModel.applyModifications()
                .addSymmetricRemoteConnection(component1, component2, 50, TransactionPropagation.NONE)
                .build();                
        
        var rewriter = new LatencyRewriter(modifiedDeploymentModel);
        var rewrittenTrace = rewriter.rewriteTrace(inputTrace);

        var artificialLocation = new SyntheticLocation(0);
        var rewrittenUseCaseStartEvent = new UseCaseStartEvent(traceId, 100, location, "uc1");
        var rewrittenServiceCandidateInvocationEvent = new ServiceCandidateInvocationEvent(traceId, 200, location, "sc1");
        var rewrittenServiceCandidateEntryEvent = new ServiceCandidateEntryEvent(traceId, 250, artificialLocation, "sc1", true, "tx1");
        var rewrittenServiceCandidateExitEvent = new ServiceCandidateExitEvent(traceId, 450, artificialLocation, "sc1");
        var rewrittenServiceCandidateReturnEvent = new ServiceCandidateReturnEvent(traceId, 500, location, "sc1");
        var rewrittenUseCaseEndEvent = new UseCaseEndEvent(traceId, 600, location, "uc1");
        
        var expectedEvents = List.<MonitoringEvent> of(
                rewrittenUseCaseStartEvent,
                rewrittenServiceCandidateInvocationEvent,
                rewrittenServiceCandidateEntryEvent,
                rewrittenServiceCandidateExitEvent,
                rewrittenServiceCandidateReturnEvent,
                rewrittenUseCaseEndEvent
                );
        
        var expectedCorrespondence = Map.<MonitoringEvent, MonitoringEvent> of(
                rewrittenUseCaseStartEvent, originalUseCaseStartEvent, //
                rewrittenServiceCandidateInvocationEvent, originalServiceCandidateInvocationEvent, //
                rewrittenServiceCandidateEntryEvent, originalServiceCandidateEntryEvent, //
                rewrittenServiceCandidateExitEvent, originalServiceCandidateExitEvent, //
                rewrittenServiceCandidateReturnEvent, originalServiceCandidateReturnEvent, //
                rewrittenUseCaseEndEvent, originalUseCaseStartEvent //
                );

        assertEquals(new RewrittenEventTrace(expectedEvents, expectedCorrespondence), rewrittenTrace);
    }

    /**
     * Test case: If a service candidate is "internalized", i.e., moved to the component in which the invoker resides, the latency must be set to zero (synthetic local connection).
     */
    @Test
    void internalizationOfServiceCandidate() {
        var traceId = 1234L;
        
        var location1 = new ObservedLocation("test", 1234, 1);
        var location2 = new ObservedLocation("test2", 1235, 2);

        var originalUseCaseStartEvent = new UseCaseStartEvent(traceId, 100, location1, "uc1");
        var originalServiceCandidateInvocationEvent = new ServiceCandidateInvocationEvent(traceId, 200, location1, "sc1");
        var originalServiceCandidateEntryEvent = new ServiceCandidateEntryEvent(traceId, 250, location2, "sc1");
        var originalServiceCandidateExitEvent = new ServiceCandidateExitEvent(traceId, 350, location2, "sc1");
        var originalServiceCandidateReturnEvent = new ServiceCandidateReturnEvent(traceId, 400, location1, "sc1");
        var originalUseCaseEndEvent = new UseCaseEndEvent(traceId, 500, location1, "uc1");
        
        var inputTrace = EventTrace.of(
                originalUseCaseStartEvent,
                originalServiceCandidateInvocationEvent,
                originalServiceCandidateEntryEvent,
                originalServiceCandidateExitEvent,
                originalServiceCandidateReturnEvent,
                originalUseCaseEndEvent
                );

        var component1 = new Component("comp1");
        var component2 = new Component("comp2");

        var useCase = new UseCase("uc1");
        var candidate = new ServiceCandidate("sc1");

        var originalDeploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component1)
                .assignServiceCandidate(candidate, component2)
                .addSymmetricRemoteConnection(component1, component2, 50, TransactionPropagation.NONE)
                .build();
        
        var modifiedDeploymentModel = originalDeploymentModel.applyModifications()
                .assignServiceCandidate(candidate, component1)
                .build();                
        
        var rewriter = new LatencyRewriter(modifiedDeploymentModel);
        var rewrittenTrace = rewriter.rewriteTrace(inputTrace);

        var rewrittenUseCaseStartEvent = new UseCaseStartEvent(traceId, 100, location1, "uc1");
        var rewrittenServiceCandidateInvocationEvent = new ServiceCandidateInvocationEvent(traceId, 200, location1, "sc1");
        var rewrittenServiceCandidateEntryEvent = new ServiceCandidateEntryEvent(traceId, 200, location1, "sc1");
        var rewrittenServiceCandidateExitEvent = new ServiceCandidateExitEvent(traceId, 300, location1, "sc1");
        var rewrittenServiceCandidateReturnEvent = new ServiceCandidateReturnEvent(traceId, 300, location1, "sc1");
        var rewrittenUseCaseEndEvent = new UseCaseEndEvent(traceId, 400, location1, "uc1");
        
        var expectedEvents = List.<MonitoringEvent> of(
                rewrittenUseCaseStartEvent,
                rewrittenServiceCandidateInvocationEvent,
                rewrittenServiceCandidateEntryEvent,
                rewrittenServiceCandidateExitEvent,
                rewrittenServiceCandidateReturnEvent,
                rewrittenUseCaseEndEvent
                );
        
        var expectedCorrespondence = Map.<MonitoringEvent, MonitoringEvent> of(
                rewrittenUseCaseStartEvent, originalUseCaseStartEvent, //
                rewrittenServiceCandidateInvocationEvent, originalServiceCandidateInvocationEvent, //
                rewrittenServiceCandidateEntryEvent, originalServiceCandidateEntryEvent, //
                rewrittenServiceCandidateExitEvent, originalServiceCandidateExitEvent, //
                rewrittenServiceCandidateReturnEvent, originalServiceCandidateReturnEvent, //
                rewrittenUseCaseEndEvent, originalUseCaseStartEvent //
                );

        assertEquals(new RewrittenEventTrace(expectedEvents, expectedCorrespondence), rewrittenTrace);
    }

}
