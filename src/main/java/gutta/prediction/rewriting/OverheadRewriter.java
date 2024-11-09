package gutta.prediction.rewriting;

import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ExplicitTransactionAbortEvent;
import gutta.prediction.event.ImplicitTransactionAbortEvent;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import gutta.prediction.simulation.TraceSimulationContext;

/**
 * This rewriter adjusts the overhead within a given trace, i.e., the difference between the timestamps of service canidate invocation/entry events as well as
 * exit/return-events according to a set of given connections.
 */
public class OverheadRewriter implements TraceRewriter {

    private final DeploymentModel deploymentModel;

    public OverheadRewriter(DeploymentModel deploymentModel) {
        this.deploymentModel = deploymentModel;
    }

    @Override
    public RewrittenEventTrace rewriteTrace(EventTrace trace) {
        return new OverheadRewriterWorker().rewriteTrace(trace, this.deploymentModel);
    }

    private static class OverheadRewriterWorker extends TraceRewriterWorker {

        private long timeOffset;

        @Override
        protected void onStartOfRewrite() {
            this.timeOffset = 0;
        }

        private long adjustTimestamp(long originalTimestamp) {
            return (originalTimestamp + this.timeOffset);
        }

        private boolean requiresRewrite(MonitoringEvent event, TraceSimulationContext context) {
            return (this.timeOffset != 0) || !(context.currentLocation().equals(event.location()));
        }

        @Override
        public void onUseCaseStartEvent(UseCaseStartEvent event, TraceSimulationContext context) {
            if (this.requiresRewrite(event, context)) {
                var rewrittenEvent = new UseCaseStartEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(), event.name());
                this.addRewrittenEvent(rewrittenEvent, event);
            } else {
                this.addRewrittenEvent(event, event);
            }
        }

        @Override
        public void onServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event, TraceSimulationContext context) {
            if (this.requiresRewrite(event, context)) {
                var rewrittenEvent = new ServiceCandidateInvocationEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(),
                        event.name());
                this.addRewrittenEvent(rewrittenEvent, event);
            } else {
                this.addRewrittenEvent(event, event);
            }
        }

        @Override
        public void beforeComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent,
                ComponentConnection connection, TraceSimulationContext context) {
            this.adjustOverhead(invocationEvent, entryEvent, connection);
        }

        @Override
        public void onServiceCandidateEntryEvent(ServiceCandidateEntryEvent event, TraceSimulationContext context) {
            if (this.requiresRewrite(event, context)) {
                var rewrittenEvent = new ServiceCandidateEntryEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(),
                        event.name(), event.transactionStarted(), event.transactionId());
                this.addRewrittenEvent(rewrittenEvent, event);
            } else {
                this.addRewrittenEvent(event, event);
            }
        }

        @Override
        public void onServiceCandidateExitEvent(ServiceCandidateExitEvent event, TraceSimulationContext context) {
            if (this.requiresRewrite(event, context)) {
                var rewrittenEvent = new ServiceCandidateExitEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(),
                        event.name());
                this.addRewrittenEvent(rewrittenEvent, event);
            } else {
                this.addRewrittenEvent(event, event);
            }
        }

        @Override
        public void beforeComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection,
                TraceSimulationContext context) {
            this.adjustOverhead(exitEvent, returnEvent, connection);
        }

        private void adjustOverhead(MonitoringEvent startEvent, MonitoringEvent endEvent, ComponentConnection connection) {
            if (connection.isModified()) {
                // For transitions over modified connections, we may need to adjust the time offset as we do not preserve the overhead
                var observedOverhead = (endEvent.timestamp() - startEvent.timestamp());
                var newOverhead = connection.overhead();

                this.timeOffset += (newOverhead - observedOverhead);
            }
        }

        @Override
        public void onServiceCandidateReturnEvent(ServiceCandidateReturnEvent event, TraceSimulationContext context) {
            if (this.requiresRewrite(event, context)) {
                var rewrittenEvent = new ServiceCandidateReturnEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(),
                        event.name());
                this.addRewrittenEvent(rewrittenEvent, event);
            } else {
                this.addRewrittenEvent(event, event);
            }
        }

        @Override
        public void onTransactionStartEvent(TransactionStartEvent event, TraceSimulationContext context) {
            if (this.requiresRewrite(event, context)) {
                var rewrittenEvent = new TransactionStartEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(),
                        event.transactionId());
                this.addRewrittenEvent(rewrittenEvent, event);
            } else {
                this.addRewrittenEvent(event, event);
            }
        }

        @Override
        public void onImplicitTransactionAbortEvent(ImplicitTransactionAbortEvent event, TraceSimulationContext context) {
            if (this.requiresRewrite(event, context)) {
                var rewrittenEvent = new ImplicitTransactionAbortEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(),
                        event.transactionId(), event.cause());
                this.addRewrittenEvent(rewrittenEvent, event);
            } else {
                this.addRewrittenEvent(event, event);
            }
        }

        @Override
        public void onExplicitTransactionAbortEvent(ExplicitTransactionAbortEvent event, TraceSimulationContext context) {
            if (this.requiresRewrite(event, context)) {
                var rewrittenEvent = new ExplicitTransactionAbortEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(),
                        event.transactionId());
                this.addRewrittenEvent(rewrittenEvent, event);
            } else {
                this.addRewrittenEvent(event, event);
            }
        }

        @Override
        public void onTransactionCommitEvent(TransactionCommitEvent event, TraceSimulationContext context) {
            if (this.requiresRewrite(event, context)) {
                var rewrittenEvent = new TransactionCommitEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(),
                        event.transactionId());
                this.addRewrittenEvent(rewrittenEvent, event);
            } else {
                this.addRewrittenEvent(event, event);
            }
        }

        @Override
        public void onEntityReadEvent(EntityReadEvent event, TraceSimulationContext context) {
            if (this.requiresRewrite(event, context)) {
                var rewrittenEvent = new EntityReadEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(), event.entity());
                this.addRewrittenEvent(rewrittenEvent, event);
            } else {
                this.addRewrittenEvent(event, event);
            }
        }

        @Override
        public void onEntityWriteEvent(EntityWriteEvent event, TraceSimulationContext context) {
            if (this.requiresRewrite(event, context)) {
                var rewrittenEvent = new EntityWriteEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(), event.entity());
                this.addRewrittenEvent(rewrittenEvent, event);
            } else {
                this.addRewrittenEvent(event, event);
            }
        }

        @Override
        public void onUseCaseEndEvent(UseCaseEndEvent event, TraceSimulationContext context) {
            if (this.requiresRewrite(event, context)) {
                var rewrittenEvent = new UseCaseEndEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), context.currentLocation(), event.name());
                this.addRewrittenEvent(rewrittenEvent, event);
            } else {
                this.addRewrittenEvent(event, event);
            }
        }

    }

}
