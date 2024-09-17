package gutta.prediction.rewriting;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.UseCase;
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
import gutta.prediction.event.TransactionStartEvent.Demarcation;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;

import java.util.Arrays;
import java.util.List;

abstract class TraceRewriterTestTemplate {
    
    protected TraceFixture createIdentityTraceFixture() {
        final var traceId = 1234L;
        final var location = new ProcessLocation("test", 1234, 1);

        var inputTrace = Arrays.<MonitoringEvent>asList(
                new UseCaseStartEvent(traceId, 100, location, "uc1"),
                new TransactionStartEvent(traceId, 200, location, "tx1", Demarcation.EXPLICIT),
                // Same timestamp for invocation and entry as to avoid latency adjustment
                new ServiceCandidateInvocationEvent(traceId, 300, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 300, location, "sc1"),
                // Again, same timestamp for exit and return
                new ServiceCandidateExitEvent(traceId, 400, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 400, location, "sc1"),
                new TransactionCommitEvent(traceId, 500, location, "tx1"),
                new TransactionStartEvent(traceId, 600, location, "tx2", Demarcation.EXPLICIT),
                new EntityReadEvent(traceId, 700, location, "et1", "id1"),
                new EntityWriteEvent(traceId, 800, location, "et1", "id1"),
                new TransactionAbortEvent(traceId, 900, location, "tx2", "NullPointerException"),
                new UseCaseEndEvent(traceId, 1000, location, "uc1")
                );

        var component = new Component("test");
        var useCase = new UseCase("uc1");
        var candidate = new ServiceCandidate("sc1", TransactionBehavior.SUPPORTED);
        
        var originalDeploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component)
                .assignServiceCandidate(candidate, component)
                .build();        
        
        return new TraceFixture(inputTrace, originalDeploymentModel, originalDeploymentModel);
    }
    
    protected record TraceFixture(List<MonitoringEvent> trace, DeploymentModel originalDeploymentModel, DeploymentModel modifiedDeploymentModel) {}

}
