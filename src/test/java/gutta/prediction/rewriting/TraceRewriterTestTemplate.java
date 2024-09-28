package gutta.prediction.rewriting;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.Entity;
import gutta.prediction.domain.EntityType;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ExplicitTransactionAbortEvent;
import gutta.prediction.event.ProcessLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;

abstract class TraceRewriterTestTemplate {
    
    protected TraceFixture createIdentityTraceFixture() {
        final var traceId = 1234L;
        final var location = new ProcessLocation("test", 1234, 1);
        var entityType = new EntityType("et1");
        var entity = new Entity(entityType, "id1");

        var inputTrace = EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location, "uc1"),
                new TransactionStartEvent(traceId, 200, location, "tx1"),
                // Same timestamp for invocation and entry as to avoid latency adjustment
                new ServiceCandidateInvocationEvent(traceId, 300, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 300, location, "sc1", false, ""),
                // Again, same timestamp for exit and return
                new ServiceCandidateExitEvent(traceId, 400, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 400, location, "sc1"),
                new TransactionCommitEvent(traceId, 500, location, "tx1"),
                new TransactionStartEvent(traceId, 600, location, "tx2"),
                new EntityReadEvent(traceId, 700, location, entity),
                new EntityWriteEvent(traceId, 800, location, entity),
                new ExplicitTransactionAbortEvent(traceId, 900, location, "tx2"),
                new UseCaseEndEvent(traceId, 1000, location, "uc1")
                );

        var component = new Component("test");
        var useCase = new UseCase("uc1");
        var candidate = new ServiceCandidate("sc1", TransactionBehavior.SUPPORTED);
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component)
                .assignServiceCandidate(candidate, component)
                .build();        
        
        return new TraceFixture(inputTrace, deploymentModel);
    }
    
    protected record TraceFixture(EventTrace trace, DeploymentModel deploymentModel) {}

}
