package gutta.prediction.rewriting;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.TransactionPropagation;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ProcessLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import gutta.prediction.stream.SyntheticLocation;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

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
        var rewrittenTrace = new LatencyRewriter(fixture.originalDeploymentModel(), fixture.modifiedDeploymentModel()).rewriteTrace(inputTrace);

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

        var rewriter = new LatencyRewriter(originalDeploymentModel, modifiedDeploymentModel);
        var rewrittenTrace = rewriter.rewriteTrace(inputTrace);

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
                new ServiceCandidateEntryEvent(traceId, 200, location, "sc1"),
                new ServiceCandidateExitEvent(traceId, 400, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 400, location, "sc1"),
                new UseCaseEndEvent(traceId, 500, location, "uc1")
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
        
        var rewriter = new LatencyRewriter(originalDeploymentModel, modifiedDeploymentModel);
        var rewrittenTrace = rewriter.rewriteTrace(inputTrace);

        var artificialLocation = new SyntheticLocation(0);
        var expectedTrace = Arrays.<MonitoringEvent>asList(
                new UseCaseStartEvent(traceId, 100, location, "uc1"),
                new ServiceCandidateInvocationEvent(traceId, 200, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 250, artificialLocation, "sc1"),
                new ServiceCandidateExitEvent(traceId, 450, artificialLocation, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 500, location, "sc1"),
                new UseCaseEndEvent(traceId, 600, location, "uc1")
                );

        assertEquals(expectedTrace, rewrittenTrace);
    }
    

}
