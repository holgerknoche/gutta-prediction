package gutta.prediction.rewriting;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.ComponentConnections;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.Location;
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * This rewriter adjusts the latencies within a given trace, i.e., the difference between the timestamps of service canidate invocation/entry events as well as exit/return-events according
 * to a set of given connections.  
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
        return new TraceRewriteWorker(events, this.useCaseAllocation, this.methodAllocation, this.connections, this::createArtificialLocation).rewriteTrace();
    }
    
    SyntheticLocation createArtificialLocation() {
        return new SyntheticLocation();
    }

    private static class TraceRewriteWorker extends TraceRewriterWorker {
        
        private final Supplier<SyntheticLocation> artificialLocationSupplier;

        private Deque<StackEntry> stack = new ArrayDeque<>();

        private String currentServiceCandidate;

        private Component currentComponent;

        private Location currentLocation;

        private long timeOffset;

        public TraceRewriteWorker(List<MonitoringEvent> events, Map<String, Component> useCaseAllocation, Map<String, Component> methodAllocation,
                ComponentConnections connections, Supplier<SyntheticLocation> artificialLocationSupplier) {
            
            super(events, useCaseAllocation, methodAllocation, connections);
            this.artificialLocationSupplier = artificialLocationSupplier;
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
        
        private SyntheticLocation createArtificialLocation() {
            return this.artificialLocationSupplier.get();
        }

        private void assertExpectedLocation(MonitoringEvent event) {
            if (this.currentLocation == null || this.currentLocation.isSynthetic()) {
                return;
            } else if (!this.currentLocation.equals(event.location())) {
                throw new IllegalStateException(
                        "Unexpected location at event '" + event + "': Expected '" + this.currentLocation + ", but found '" + event.location() + "'.");    
            }
        }

        @Override
        public Void handleUseCaseStartEvent(UseCaseStartEvent event) {
            this.assertExpectedLocation(event);

            var useCaseName = event.name();            

            // Determine the component providing the given use case
            var component = this.determineComponentForUseCase(useCaseName);

            this.currentComponent = component;
            this.currentLocation = event.location();

            var rewrittenEvent = new UseCaseStartEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation, event.name());
            return this.addRewrittenEvent(rewrittenEvent);
        }
                
        @Override
        public Void handleServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event) {
            this.assertExpectedLocation(event);

            var rewrittenEvent = new ServiceCandidateInvocationEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), event.location(), event.name());

            this.processCandidateInvocation(this.currentComponent, event, this::performTransitionToCandidate);
            return this.addRewrittenEvent(rewrittenEvent);            
        }
        
        private void performTransitionToCandidate(MonitoringEvent transitionStartEvent, ServiceCandidateEntryEvent transitionEndEvent, ComponentConnection connection) {
            // Save the current state on the stack before making changes
            this.stack.push(new StackEntry(this.currentServiceCandidate, this.currentComponent, this.currentLocation));

            var targetComponent = connection.target();
            if (connection.isSynthetic()) {
                this.performSimulatedTransition(transitionStartEvent, transitionEndEvent, targetComponent, connection);
            } else {
                this.performObservedTransition(transitionStartEvent, transitionEndEvent, targetComponent, connection);
            }
                        
            this.currentServiceCandidate = transitionEndEvent.name();
            this.currentComponent = targetComponent;
        }
        
        private void performSimulatedTransition(MonitoringEvent transitionStartEvent, MonitoringEvent transitionEndEvent, Component targetComponent, ComponentConnection connection) {
            // For simulated transitions, we may need to adjust the time offset as we do not preserve the latency
            var observedLatency = (transitionEndEvent.timestamp() - transitionStartEvent.timestamp());
            var newLatency = connection.latency();
            
            this.timeOffset += (newLatency - observedLatency);
        
            if (connection.isRemote()) {
                // For remote transitions, we move to an artificial location that is guaranteed to be different from all others
                // TODO Affinities, i.e., return to the same location for a later invocation
                var newLocation = this.createArtificialLocation();
                this.currentLocation = newLocation;
            }
        }
                
        private void performObservedTransition(MonitoringEvent transitionStartEvent, MonitoringEvent transitionEndEvent, Component targetComponent, ComponentConnection connection) {
            var sourceLocation = transitionStartEvent.location();
            var targetLocation = transitionEndEvent.location();
            
            var locationChanged = !sourceLocation.equals(targetLocation);
            
            if (connection.isRemote() && !locationChanged) {
                // Remote connections are expected to change the location
                throw new IllegalStateException("Remote invocation without change of location detected.");
            } else if (!connection.isRemote() && locationChanged) {
                // Location changes with non-remote connections are inadmissible
                throw new IllegalStateException("Change of location with non-remote invocation detected.");
            }
            
            this.currentLocation = targetLocation;
        }
        
        @Override
        public Void handleServiceCandidateEntryEvent(ServiceCandidateEntryEvent event) {
            this.assertExpectedLocation(event);

            var rewrittenEvent = new ServiceCandidateEntryEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation, event.name());            
            return this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public Void handleServiceCandidateExitEvent(ServiceCandidateExitEvent event) {
            this.assertExpectedLocation(event);

            var rewrittenEvent = new ServiceCandidateExitEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation, event.name());

            var nextEvent = this.lookahead(1);
            if (nextEvent instanceof ServiceCandidateReturnEvent returnEvent) {
                var sourceComponent = this.currentComponent;
                // Determine the component to return to from the top of the stack
                var targetComponent = this.stack.peek().component();
                var connection = this.determineConnectionBetween(sourceComponent, targetComponent);

                if (connection.isSynthetic()) {
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
            
            var stackEntry = this.stack.pop();
            this.currentServiceCandidate = stackEntry.methodName();
            this.currentComponent = stackEntry.component();
            this.currentLocation = stackEntry.location();

            var rewrittenEvent = new ServiceCandidateReturnEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation, event.name());
            return this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public Void handleTransactionStartEvent(TransactionStartEvent event) {
            this.assertExpectedLocation(event);

            var rewrittenEvent = new TransactionStartEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation, event.transactionId(), event.demarcation());
            return this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public Void handleTransactionAbortEvent(TransactionAbortEvent event) {
            this.assertExpectedLocation(event);

            var rewrittenEvent = new TransactionAbortEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation, event.transactionId(),
                    event.cause());
            return this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public Void handleTransactionCommitEvent(TransactionCommitEvent event) {
            this.assertExpectedLocation(event);

            var rewrittenEvent = new TransactionCommitEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation, event.transactionId());
            return this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public Void handleEntityReadEvent(EntityReadEvent event) {
            this.assertExpectedLocation(event);

            var rewrittenEvent = new EntityReadEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation, event.entityType(),
                    event.entityIdentifier());
            return this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public Void handleEntityWriteEvent(EntityWriteEvent event) {
            this.assertExpectedLocation(event);

            var rewrittenEvent = new EntityWriteEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation, event.entityType(),
                    event.entityIdentifier());
            return this.addRewrittenEvent(rewrittenEvent);
        }

        @Override
        public Void handleUseCaseEndEvent(UseCaseEndEvent event) {
            this.assertExpectedLocation(event);

            var rewrittenEvent = new UseCaseEndEvent(event.traceId(), this.adjustTimestamp(event.timestamp()), this.currentLocation, event.name());

            this.currentServiceCandidate = null;
            this.currentComponent = null;
            this.currentLocation = null;

            return this.addRewrittenEvent(rewrittenEvent);
        }

    }

    private record StackEntry(String methodName, Component component, Location location) {}
    
}
