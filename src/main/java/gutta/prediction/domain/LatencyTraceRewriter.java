package gutta.prediction.domain;

import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.MonitoringEventVisitor;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionAbortEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class LatencyTraceRewriter {

    public List<MonitoringEvent> rewriteTrace(List<MonitoringEvent> events, Map<String, Component> useCaseAllocation, Map<String, Component> methodAllocation,
            ComponentConnections connections) {
        return new TraceRewriteWorker(events, useCaseAllocation, methodAllocation, connections).rewriteTrace();
    }

    private static class TraceRewriteWorker implements MonitoringEventVisitor<Void> {

        private final EventStream events;

        private final Map<String, Component> useCaseAllocation;

        private final Map<String, Component> methodAllocation;

        private final ComponentConnections connections;

        private List<MonitoringEvent> rewrittenEvents;

        private Deque<StackEntry> stack = new ArrayDeque<>();

        private String currentServiceCandidate;

        private Component currentComponent;

        private Location currentLocation;

        private long timeOffset;

        public TraceRewriteWorker(List<MonitoringEvent> events, Map<String, Component> useCaseAllocation, Map<String, Component> methodAllocation,
                ComponentConnections connections) {
            this.events = new EventStream(events);

            this.useCaseAllocation = useCaseAllocation;
            this.methodAllocation = methodAllocation;
            this.connections = connections;
        }

        public List<MonitoringEvent> rewriteTrace() {
            this.rewrittenEvents = new ArrayList<>(this.events.size());
            this.timeOffset = 0;

            while (true) {
                var currentEvent = this.events.lookahead(0);
                if (currentEvent == null) {
                    break;
                }

                currentEvent.accept(this);
                this.events.consume();
            }

            return this.rewrittenEvents;
        }

        private long adjustTimestamp(long originalTimestamp) {
            return (originalTimestamp + this.timeOffset);
        }

        private Void addRewrittenEvent(MonitoringEvent event) {
            this.rewrittenEvents.add(event);
            return null;
        }

        private void assertExpectedLocation(MonitoringEvent event) {
            if (this.currentLocation != null && !this.currentLocation.equals(event.location())) {
                throw new IllegalStateException(
                        "Unexpected location at event '" + event + "': Expected '" + this.currentLocation + ", but found '" + event.location() + "'.");
            }
        }

        @Override
        public Void handleUseCaseStartEvent(UseCaseStartEvent event) {
            this.assertExpectedLocation(event);

            var useCaseName = event.name();
            var rewrittenEvent = new UseCaseStartEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), event.location(), event.name());

            // Determine the component providing the given use case
            var component = this.useCaseAllocation.get(useCaseName);
            if (component == null) {
                throw new IllegalStateException("Use case '" + useCaseName + "' is not assigned to a component.");
            }

            this.currentComponent = component;
            this.currentLocation = event.location();

            return this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public Void handleServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event) {
            this.assertExpectedLocation(event);

            var rewrittenEvent = new ServiceCandidateInvocationEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), event.location(), event.name());

            var nextEvent = this.events.lookahead(1);
            if (nextEvent instanceof ServiceCandidateEntryEvent entryEvent) {
                var sourceComponent = this.currentComponent;

                var invokedCandidateName = entryEvent.name();
                var targetComponent = this.methodAllocation.get(invokedCandidateName);
                if (targetComponent == null) {
                    throw new IllegalStateException("Service candidate '" + invokedCandidateName + "' is not assigned to a component.");
                }

                var connection = this.connections.getConnection(sourceComponent, targetComponent)
                        .orElseThrow(() -> new IllegalStateException("No connection from '" + sourceComponent + "' to '" + targetComponent + "'."));

                if (connection.modified()) {
                    // If the connection has been modified, adjust the time offset accordingly
                    var existingLatency = (entryEvent.timestamp() - event.timestamp());
                    var newLatency = connection.latency();
                    this.timeOffset += (newLatency - existingLatency);
                }

                return this.addRewrittenEvent(rewrittenEvent);
            } else {
                throw new IllegalStateException("A service candidate invocation event is not followed by a service candidate entry event.");
            }
        }

        @Override
        public Void handleServiceCandidateEntryEvent(ServiceCandidateEntryEvent event) {
            this.assertExpectedLocation(event);

            var rewrittenEvent = new ServiceCandidateEntryEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), event.location(), event.name());

            this.stack.push(new StackEntry(this.currentServiceCandidate, this.currentComponent, this.currentLocation));
            this.currentServiceCandidate = event.name();
            this.currentComponent = this.methodAllocation.get(event.name());
            this.currentLocation = event.location();

            return this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public Void handleServiceCandidateExitEvent(ServiceCandidateExitEvent event) {
            this.assertExpectedLocation(event);

            var rewrittenEvent = new ServiceCandidateExitEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), event.location(), event.name());

            var nextEvent = this.events.lookahead(1);
            if (nextEvent instanceof ServiceCandidateReturnEvent returnEvent) {
                var sourceComponent = this.currentComponent;
                // Determine the component to return to from the top of the stack
                var targetComponent = this.stack.peek().component();

                var connection = this.connections.getConnection(sourceComponent, targetComponent)
                        .orElseThrow(() -> new IllegalStateException("No connection from '" + sourceComponent + "' to '" + targetComponent + "'."));

                if (connection.modified()) {
                    // Determine the new latency and adjust the time offset accordingly
                    var existingLatency = (returnEvent.timestamp() - event.timestamp());
                    var newLatency = connection.latency();
                    this.timeOffset += (newLatency - existingLatency);
                }

                return this.addRewrittenEvent(rewrittenEvent);
            } else {
                throw new IllegalStateException("A service candidate exit event is not followed by a service candidate return event.");
            }
        }

        @Override
        public Void handleServiceCandidateReturnEvent(ServiceCandidateReturnEvent event) {
            this.assertExpectedLocation(event);

            var rewrittenEvent = new ServiceCandidateReturnEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), event.location(), event.name());

            var stackEntry = this.stack.pop();
            this.currentServiceCandidate = stackEntry.methodName();
            this.currentComponent = stackEntry.component();
            this.currentLocation = stackEntry.location();

            return this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public Void handleTransactionStartEvent(TransactionStartEvent event) {
            this.assertExpectedLocation(event);

            var rewrittenEvent = new TransactionStartEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), event.location(), event.transactionId());
            return this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public Void handleTransactionAbortEvent(TransactionAbortEvent event) {
            this.assertExpectedLocation(event);

            var rewrittenEvent = new TransactionAbortEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), event.location(), event.transactionId(),
                    event.cause());
            return this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public Void handleTransactionCommitEvent(TransactionCommitEvent event) {
            this.assertExpectedLocation(event);

            var rewrittenEvent = new TransactionCommitEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), event.location(), event.transactionId());
            return this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public Void handleEntityReadEvent(EntityReadEvent event) {
            this.assertExpectedLocation(event);

            var rewrittenEvent = new EntityReadEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), event.location(), event.entityType(),
                    event.entityIdentifier());
            return this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public Void handleEntityWriteEvent(EntityWriteEvent event) {
            this.assertExpectedLocation(event);

            var rewrittenEvent = new EntityWriteEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), event.location(), event.entityType(),
                    event.entityIdentifier());
            return this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public Void handleUseCaseEndEvent(UseCaseEndEvent event) {
            this.assertExpectedLocation(event);

            var rewrittenEvent = new UseCaseEndEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), event.location(), event.name());

            this.currentServiceCandidate = null;
            this.currentComponent = null;
            this.currentLocation = null;

            return this.addRewrittenEvent(rewrittenEvent);
        }

    }

    private static class EventStream {

        private final List<MonitoringEvent> events;

        private int maxPosition;

        private int currentPosition;

        public EventStream(List<MonitoringEvent> events) {
            this.events = events;
            this.maxPosition = (events.size() - 1);
        }

        public int size() {
            return this.events.size();
        }

        public MonitoringEvent lookahead(int amount) {
            var desiredPosition = (this.currentPosition + amount);
            if (desiredPosition > this.maxPosition) {
                return null;
            }

            return this.events.get(desiredPosition);
        }

        public void consume() {
            if (this.currentPosition <= this.maxPosition) {
                this.currentPosition++;
            }
        }

    }

    private record StackEntry(String methodName, Component component, Location location) {
    }

}
