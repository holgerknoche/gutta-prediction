package gutta.prediction.rewriting;

import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
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
import gutta.prediction.stream.EventProcessingContext;

import java.util.List;

/**
 * This rewriter adjusts the latencies within a given trace, i.e., the difference between the timestamps of service canidate invocation/entry events as well as
 * exit/return-events according to a set of given connections.
 */
public class LatencyRewriter implements TraceRewriter {

    private final DeploymentModel originalDeploymentModel;
    
    private final DeploymentModel modifiedDeploymentModel;

    public LatencyRewriter(DeploymentModel originalDeploymentModel, DeploymentModel modifiedDeploymentModel) {
        this.originalDeploymentModel = originalDeploymentModel;
        this.modifiedDeploymentModel = modifiedDeploymentModel;
    }

    @Override
    public List<MonitoringEvent> rewriteTrace(List<MonitoringEvent> trace) {
        return new LatencyRewriterWorker().rewriteTrace(trace, this.originalDeploymentModel, this.modifiedDeploymentModel);        
    }
    
    private static class LatencyRewriterWorker extends TraceRewriterWorker {        

        private long timeOffset;
        
        @Override
        protected void onStartOfRewrite() {
            this.timeOffset = 0;
        }

        private long adjustTimestamp(long originalTimestamp) {
            return (originalTimestamp + this.timeOffset);
        }

        @Override
        public void onUseCaseStartEvent(UseCaseStartEvent event, EventProcessingContext context) {
            var rewrittenEvent = new UseCaseStartEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(), event.name());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public void onServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event, EventProcessingContext context) {
            var rewrittenEvent = new ServiceCandidateInvocationEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(), event.name());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public void onComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ComponentConnection connection, EventProcessingContext context) {
            this.adjustLatency(invocationEvent, entryEvent, connection);
        }

        @Override
        public void onServiceCandidateEntryEvent(ServiceCandidateEntryEvent event, EventProcessingContext context) {
            var rewrittenEvent = new ServiceCandidateEntryEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(), event.name());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public void onServiceCandidateExitEvent(ServiceCandidateExitEvent event, EventProcessingContext context) {
            var rewrittenEvent = new ServiceCandidateExitEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(), event.name());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public void onComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection, EventProcessingContext context) {
            this.adjustLatency(exitEvent, returnEvent, connection);
        }
        
        private void adjustLatency(MonitoringEvent startEvent, MonitoringEvent endEvent, ComponentConnection connection) {
            if (connection.isSynthetic()) {
                // For transitions over synthetic connections, we may need to adjust the time offset as we do not preserve the latency
                var observedLatency = (endEvent.timestamp() - startEvent.timestamp());
                var newLatency = connection.latency();

                this.timeOffset += (newLatency - observedLatency);
            }
        }

        @Override
        public void onServiceCandidateReturnEvent(ServiceCandidateReturnEvent event, EventProcessingContext context) {
            var rewrittenEvent = new ServiceCandidateReturnEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(), event.name());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public void onTransactionStartEvent(TransactionStartEvent event, EventProcessingContext context) {
            var rewrittenEvent = new TransactionStartEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(), event.transactionId(), event.demarcation());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public void onTransactionAbortEvent(TransactionAbortEvent event, EventProcessingContext context) {
            var rewrittenEvent = new TransactionAbortEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(), event.transactionId(), event.cause());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public void onTransactionCommitEvent(TransactionCommitEvent event, EventProcessingContext context) {
            var rewrittenEvent = new TransactionCommitEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(), event.transactionId());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public void onEntityReadEvent(EntityReadEvent event, EventProcessingContext context) {
            var rewrittenEvent = new EntityReadEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(), event.entityType(), event.entityIdentifier());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public void onEntityWriteEvent(EntityWriteEvent event, EventProcessingContext context) {
            var rewrittenEvent = new EntityWriteEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(), event.entityType(), event.entityIdentifier());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public void onUseCaseEndEvent(UseCaseEndEvent event, EventProcessingContext context) {
            var rewrittenEvent = new UseCaseEndEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(), event.name());
            this.addRewrittenEvent(rewrittenEvent);
        }

    }

}
