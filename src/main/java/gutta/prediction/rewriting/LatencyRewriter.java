package gutta.prediction.rewriting;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.ComponentConnections;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This rewriter adjusts the latencies within a given trace, i.e., the difference between the timestamps of service canidate invocation/entry events as well as
 * exit/return-events according to a set of given connections.
 */
public class LatencyRewriter implements TraceRewriter {

    private final Map<String, Component> useCaseAllocation;

    private final Map<String, Component> methodAllocation;

    private final ComponentConnections connections;

    public LatencyRewriter(Map<String, Component> useCaseAllocation, Map<String, Component> methodAllocation, ComponentConnections connections) {
        this.useCaseAllocation = useCaseAllocation;
        this.methodAllocation = methodAllocation;
        this.connections = connections;
    }

    @Override
    public List<MonitoringEvent> rewriteTrace(List<MonitoringEvent> events) {
        return this.createWorker(events, this.useCaseAllocation, this.methodAllocation, this.connections).rewriteTrace();
    }
    
    LatencyRewriterWorker createWorker(List<MonitoringEvent> events, Map<String, Component> useCaseAllocation, Map<String, Component> methodAllocation, ComponentConnections connections) {
        return new LatencyRewriterWorker(events, useCaseAllocation, methodAllocation, connections); 
    }

    static class LatencyRewriterWorker extends TraceRewriterWorker {        

        private long timeOffset;

        public LatencyRewriterWorker(List<MonitoringEvent> events, Map<String, Component> useCaseAllocation, Map<String, Component> methodAllocation, ComponentConnections connections) {
            super(events, useCaseAllocation, methodAllocation, connections);
        }

        public List<MonitoringEvent> rewriteTrace() {
            this.rewrittenEvents = new ArrayList<>();
            this.timeOffset = 0;

            this.processEvents();

            return this.rewrittenEvents;
        }

        private long adjustTimestamp(long originalTimestamp) {
            return (originalTimestamp + this.timeOffset);
        }

        @Override
        protected void onUseCaseStartEvent(UseCaseStartEvent event) {
            var rewrittenEvent = new UseCaseStartEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation(), event.name());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        protected void onServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event) {
            var rewrittenEvent = new ServiceCandidateInvocationEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation(), event.name());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        protected void onComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ComponentConnection connection) {
            this.adjustLatency(invocationEvent, entryEvent, connection);
        }

        @Override
        protected void onServiceCandidateEntryEvent(ServiceCandidateEntryEvent event) {
            var rewrittenEvent = new ServiceCandidateEntryEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation(), event.name());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        protected void onServiceCandidateExitEvent(ServiceCandidateExitEvent event) {
            var rewrittenEvent = new ServiceCandidateExitEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation(), event.name());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        protected void onComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection) {
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
        protected void onServiceCandidateReturnEvent(ServiceCandidateReturnEvent event) {
            var rewrittenEvent = new ServiceCandidateReturnEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation(), event.name());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        protected void onTransactionStartEvent(TransactionStartEvent event) {
            var rewrittenEvent = new TransactionStartEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation(), event.transactionId(), event.demarcation());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        protected void onTransactionAbortEvent(TransactionAbortEvent event) {
            var rewrittenEvent = new TransactionAbortEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation(), event.transactionId(), event.cause());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        protected void onTransactionCommitEvent(TransactionCommitEvent event) {
            var rewrittenEvent = new TransactionCommitEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation(), event.transactionId());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        protected void onEntityReadEvent(EntityReadEvent event) {
            var rewrittenEvent = new EntityReadEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation(), event.entityType(), event.entityIdentifier());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        protected void onEntityWriteEvent(EntityWriteEvent event) {
            var rewrittenEvent = new EntityWriteEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation(), event.entityType(), event.entityIdentifier());
            this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        protected void onUseCaseEndEvent(UseCaseEndEvent event) {
            var rewrittenEvent = new UseCaseEndEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation(), event.name());
            this.addRewrittenEvent(rewrittenEvent);
        }

    }

}
