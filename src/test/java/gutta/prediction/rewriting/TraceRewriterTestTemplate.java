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
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ObservedLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;

import java.util.HashMap;

abstract class TraceRewriterTestTemplate {
    
    protected TraceFixture createIdentityTraceFixture() {
        final var traceId = 1234L;
        final var location = new ObservedLocation("test", 1234, 1);
        var entityType = new EntityType("et1");
        var entity = new Entity(entityType, "id1");

        var useCaseStartEvent = new UseCaseStartEvent(traceId, 100, location, "uc1");
        var transactionStartEvent1 = new TransactionStartEvent(traceId, 200, location, "tx1");
        // Same timestamp for invocation and entry as to avoid latency adjustment
        var serviceCandidateInvocationEvent = new ServiceCandidateInvocationEvent(traceId, 300, location, "sc1");
        var serviceCandidateEntryEvent = new ServiceCandidateEntryEvent(traceId, 300, location, "sc1", false, "");
        // Again, same timestamp for exit and return
        var serviceCandidateExitEvent = new ServiceCandidateExitEvent(traceId, 400, location, "sc1");
        var serviceCandidateReturnEvent = new ServiceCandidateReturnEvent(traceId, 400, location, "sc1");
        var transactionCommitEvent = new TransactionCommitEvent(traceId, 500, location, "tx1");
        var transactionStartEvent2 = new TransactionStartEvent(traceId, 600, location, "tx2");
        var entityReadEvent = new EntityReadEvent(traceId, 700, location, entity);
        var entityWriteEvent = new EntityWriteEvent(traceId, 800, location, entity);
        var transactionAbortEvent = new ExplicitTransactionAbortEvent(traceId, 900, location, "tx2");
        var useCaseEndEvent = new UseCaseEndEvent(traceId, 1000, location, "uc1");
        
        var inputTrace = EventTrace.of(
                useCaseStartEvent,
                transactionStartEvent1,
                // Same timestamp for invocation and entry as to avoid latency adjustment
                serviceCandidateInvocationEvent,
                serviceCandidateEntryEvent,
                // Again, same timestamp for exit and return
                serviceCandidateExitEvent,
                serviceCandidateReturnEvent,
                transactionCommitEvent,
                transactionStartEvent2,
                entityReadEvent,
                entityWriteEvent,
                transactionAbortEvent,
                useCaseEndEvent
                );

        var component = new Component("test");
        var useCase = new UseCase("uc1");
        var candidate = new ServiceCandidate("sc1", TransactionBehavior.SUPPORTED);
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCaseToComponent(useCase, component)
                .assignServiceCandidateToComponent(candidate, component)
                .build();        
        
        var correspondence = new HashMap<MonitoringEvent, MonitoringEvent>();
        correspondence.put(useCaseStartEvent, useCaseStartEvent);
        correspondence.put(transactionStartEvent1, transactionStartEvent1);
        correspondence.put(serviceCandidateInvocationEvent, serviceCandidateInvocationEvent);
        correspondence.put(serviceCandidateEntryEvent, serviceCandidateEntryEvent);
        correspondence.put(serviceCandidateExitEvent, serviceCandidateExitEvent);
        correspondence.put(serviceCandidateReturnEvent, serviceCandidateReturnEvent);
        correspondence.put(transactionCommitEvent, transactionCommitEvent);
        correspondence.put(transactionStartEvent2, transactionStartEvent2);
        correspondence.put(entityReadEvent, entityReadEvent);
        correspondence.put(entityWriteEvent, entityWriteEvent);
        correspondence.put(transactionAbortEvent, transactionAbortEvent);
        correspondence.put(useCaseEndEvent, useCaseEndEvent);
        
        return new TraceFixture(inputTrace, deploymentModel, new RewrittenEventTrace(inputTrace.events(), correspondence));
    }
    
    protected record TraceFixture(EventTrace trace, DeploymentModel deploymentModel, RewrittenEventTrace rewrittenTrace) {}

}
