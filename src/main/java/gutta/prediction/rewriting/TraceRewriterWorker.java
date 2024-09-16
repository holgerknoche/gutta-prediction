package gutta.prediction.rewriting;

import gutta.prediction.common.AbstractMonitoringEventProcessor;
import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.ComponentConnections;
import gutta.prediction.domain.ServiceCandidate;
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
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class TraceRewriterWorker extends AbstractMonitoringEventProcessor {
    
    private final Map<String, ServiceCandidate> nameToCandidate;
    
    private Deque<StackEntry> stack = new ArrayDeque<>();

    private ServiceCandidate currentServiceCandidate;

    private Component currentComponent;

    private Location currentLocation;
    
    private List<MonitoringEvent> rewrittenEvents;

    protected TraceRewriterWorker(List<MonitoringEvent> events, List<ServiceCandidate> serviceCandidates, Map<String, Component> useCaseAllocation, Map<ServiceCandidate, Component> methodAllocation,
            ComponentConnections connections) {

        super(events, useCaseAllocation, methodAllocation, connections);
        
        this.nameToCandidate = serviceCandidates.stream().collect(Collectors.toMap(ServiceCandidate::name, Function.identity()));
    }
    
    public List<MonitoringEvent> rewriteTrace() {
        this.rewrittenEvents = new ArrayList<>();

        this.onStartOfRewrite();
        this.processEvents();
        this.onEndOfRewrite();

        return this.rewrittenEvents;
    }
    
    protected void onStartOfRewrite() {
        // Do nothing by default
    }
    
    protected void onEndOfRewrite() {
     // Do nothing by default
    }
    
    protected ServiceCandidate currentServiceCandidate() {
        return this.currentServiceCandidate;
    }
    
    protected Component currentComponent() {
        return this.currentComponent;
    }
    
    protected Location currentLocation() {
        return this.currentLocation;
    }
    
    protected void addRewrittenEvent(MonitoringEvent event) {
        this.rewrittenEvents.add(event);
    }
        
    protected void copyUnchanged(MonitoringEvent event) {
        this.addRewrittenEvent(event);
    }
    
    protected SyntheticLocation createSyntheticLocation() {
        return new SyntheticLocation();
    }
        
    private void assertExpectedLocation(MonitoringEvent event) {
        if (this.currentLocation == null || this.currentLocation.isSynthetic()) {
            return;
        } else if (!this.currentLocation.equals(event.location())) {
            throw new IllegalStateException(
                    "Unexpected location at event '" + event + "': Expected '" + this.currentLocation + ", but found '" + event.location() + "'.");    
        }
    }
    
    private ServiceCandidate resolveCandidate(String name) {
        var candidate = this.nameToCandidate.get(name);
        if (candidate == null) {
            throw new IllegalArgumentException("No service candidate with name '" + name + "'.");
        }
        
        return candidate;
    }
    
    @Override
    public final Void handleEntityReadEvent(EntityReadEvent event) {
        this.assertExpectedLocation(event);
        this.onEntityReadEvent(event);
        return null;
    }
    
    protected void onEntityReadEvent(EntityReadEvent event) {
        // By default, copy the event without making any changes 
        this.copyUnchanged(event);
    }
    
    @Override
    public final Void handleEntityWriteEvent(EntityWriteEvent event) {
        this.assertExpectedLocation(event);
        this.onEntityWriteEvent(event);
        return null;
    }
    
    protected void onEntityWriteEvent(EntityWriteEvent event) {
        // By default, copy the event without making any changes 
        this.copyUnchanged(event);
    }
    
    @Override
    public final Void handleServiceCandidateEntryEvent(ServiceCandidateEntryEvent event) {
        this.assertExpectedLocation(event);
        this.onServiceCandidateEntryEvent(event);
        return null;
    }
    
    protected void onServiceCandidateEntryEvent(ServiceCandidateEntryEvent event) {
        // By default, copy the event without making any changes 
        this.copyUnchanged(event);
    }
        
    @Override
    public final Void handleServiceCandidateExitEvent(ServiceCandidateExitEvent event) {
        this.assertExpectedLocation(event);
        this.onServiceCandidateExitEvent(event);
        
        var nextEvent = this.lookahead(1);
        if (nextEvent instanceof ServiceCandidateReturnEvent returnEvent) {
            var sourceComponent = this.currentComponent;
            // Determine the component to return to from the top of the stack
            var targetComponent = this.stack.peek().component();
            var connection = this.determineConnectionBetween(sourceComponent, targetComponent);

            this.onComponentReturn(event, returnEvent, connection);
        } else {
            throw new IllegalStateException("A service candidate exit event is not followed by a service candidate return event.");
        }
        
        return null;
    }
    
    protected void onServiceCandidateExitEvent(ServiceCandidateExitEvent event) {
        // By default, copy the event without making any changes 
        this.copyUnchanged(event);
    }
    
    protected void onComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection) {
        // Do nothing by default
    }
    
    @Override
    public final Void handleServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event) {
        this.assertExpectedLocation(event);
        this.onServiceCandidateInvocationEvent(event);
        
        var nextEvent = this.lookahead(1);
        if (nextEvent instanceof ServiceCandidateEntryEvent entryEvent) {
            var sourceComponent = this.currentComponent; 
            var invokedCandidate = this.resolveCandidate(entryEvent.name());
            var targetComponent = this.determineComponentForServiceCandidate(invokedCandidate);                
            var connection = this.determineConnectionBetween(sourceComponent, targetComponent);

            this.performComponentTransition(event, entryEvent, connection);
        } else {
            throw new IllegalStateException("A service candidate invocation event is not followed by a service candidate entry event.");
        }

        return null;
    }
    
    protected void onServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event) {
        // By default, copy the event without making any changes 
        this.copyUnchanged(event);
    }
    
    private void performComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ComponentConnection connection) {
        this.onComponentTransition(invocationEvent, entryEvent, connection);
        
        // Save the current state on the stack before making changes
        this.stack.push(new StackEntry(this.currentServiceCandidate, this.currentComponent, this.currentLocation));

        var targetComponent = connection.target();
        var sourceLocation = invocationEvent.location();
        var targetLocation = entryEvent.location();
        
        if (connection.isRemote() && connection.isSynthetic() && !targetLocation.isSynthetic()) {
            // For transitions along synthetic remote connections, a synthetic target location is required.
            // If the target location is not already synthetic (for instance, due to a previous rewrite), it is created.
            targetLocation = this.createSyntheticLocation();            
        }

        // Ensure that the transition is valid
        this.ensureValidLocationTransition(sourceLocation, targetLocation, connection);
                    
        this.currentServiceCandidate = this.resolveCandidate(entryEvent.name());
        this.currentComponent = targetComponent;
        this.currentLocation = targetLocation;
    }   
    
    private void ensureValidLocationTransition(Location sourceLocation, Location targetLocation, ComponentConnection connection) {
        var locationChanged = !sourceLocation.equals(targetLocation);
        
        if (connection.isRemote() && !locationChanged) {
            // Remote connections are expected to change the location
            throw new IllegalStateException("Remote invocation without change of location detected.");
        } else if (!connection.isRemote() && locationChanged) {
            // Location changes with non-remote connections are inadmissible
            throw new IllegalStateException("Change of location with non-remote invocation detected.");
        }
    }
        
    protected void onComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ComponentConnection connection) {
        // Do nothing by default
    }
    
    @Override
    public final Void handleServiceCandidateReturnEvent(ServiceCandidateReturnEvent event) {
        this.assertExpectedLocation(event);        
        
        var stackEntry = this.stack.pop();
        this.currentServiceCandidate = stackEntry.serviceCandidate();
        this.currentComponent = stackEntry.component();
        this.currentLocation = stackEntry.location();

        this.onServiceCandidateReturnEvent(event);
        
        return null;
    }
        
    protected void onServiceCandidateReturnEvent(ServiceCandidateReturnEvent event) {
        // By default, copy the event without making any changes 
        this.copyUnchanged(event);        
    }
    
    @Override
    public final Void handleTransactionAbortEvent(TransactionAbortEvent event) {
        this.assertExpectedLocation(event);
        this.onTransactionAbortEvent(event);              
        return null;
    }
    
    protected void onTransactionAbortEvent(TransactionAbortEvent event) {
        // By default, copy the event without making any changes 
        this.copyUnchanged(event);        
    }
    
    @Override
    public final Void handleTransactionCommitEvent(TransactionCommitEvent event) {
        this.assertExpectedLocation(event);
        this.onTransactionCommitEvent(event);              
        return null;
    }
    
    protected void onTransactionCommitEvent(TransactionCommitEvent event) {
        // By default, copy the event without making any changes 
        this.copyUnchanged(event);
    }
    
    @Override
    public final Void handleTransactionStartEvent(TransactionStartEvent event) {
        this.assertExpectedLocation(event);
        this.onTransactionStartEvent(event);              
        return null;
    }
    
    protected void onTransactionStartEvent(TransactionStartEvent event) {
        // By default, copy the event without making any changes 
        this.copyUnchanged(event);
    }
    
    @Override
    public final Void handleUseCaseStartEvent(UseCaseStartEvent event) {
        this.assertExpectedLocation(event);                
        
        // Determine the component providing the given use case
        var useCaseName = event.name();            
        var component = this.determineComponentForUseCase(useCaseName);

        this.currentComponent = component;
        this.currentLocation = event.location();
               
        this.onUseCaseStartEvent(event);        
        return null;
    }
    
    protected void onUseCaseStartEvent(UseCaseStartEvent event) {
        // By default, copy the event without making any changes 
        this.copyUnchanged(event);        
    }
    
    @Override
    public final Void handleUseCaseEndEvent(UseCaseEndEvent event) {
        this.assertExpectedLocation(event);
        this.onUseCaseEndEvent(event);
        
        this.currentServiceCandidate = null;
        this.currentComponent = null;
        this.currentLocation = null;
        
        return null;
    }
    
    protected void onUseCaseEndEvent(UseCaseEndEvent event) {
        // By default, copy the event without making any changes 
        this.copyUnchanged(event);        
    }
    
    private record StackEntry(ServiceCandidate serviceCandidate, Component component, Location location) {}

}
