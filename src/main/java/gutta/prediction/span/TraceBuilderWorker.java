package gutta.prediction.span;

import gutta.prediction.analysis.consistency.ConsistencyIssue;
import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import gutta.prediction.simulation.TraceSimulationContext;
import gutta.prediction.simulation.TraceSimulationListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static gutta.prediction.simulation.TraceSimulator.runSimulationOf;

class TraceBuilderWorker implements TraceSimulationListener {

    private final Map<Location, Span> locationToSpan = new HashMap<>();

    private long traceId;
    
    private String traceName;
    
    private Span rootSpan;
    
    private Span currentSpan;

    public Trace buildTrace(EventTrace eventTrace, DeploymentModel deploymentModel, Set<ConsistencyIssue<?>> consistencyIssues) {
        runSimulationOf(eventTrace, deploymentModel, this);

        return new Trace(this.traceId, this.traceName, this.rootSpan);
    }

    @Override
    public void onUseCaseStartEvent(UseCaseStartEvent event, TraceSimulationContext context) {
        this.traceId = event.traceId();
        this.traceName = event.name();
        
        var newSpan = new Span(event.name(), event.timestamp(), null);        
        
        this.rootSpan = newSpan;
        this.currentSpan = newSpan;
        this.locationToSpan.put(event.location(), newSpan);
    }

    private static boolean locationChange(MonitoringEvent firstEvent, MonitoringEvent secondEvent) {
        return !(firstEvent.location().equals(secondEvent.location()));
    }

    @Override
    public void onComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ComponentConnection connection,
            TraceSimulationContext context) {

        this.onComponentChange(invocationEvent, entryEvent, entryEvent.name());
    }
    
    private void onComponentChange(MonitoringEvent sourceEvent, MonitoringEvent targetEvent, String targetName) {
        if (locationChange(sourceEvent, targetEvent)) {
            this.currentSpan = this.locationToSpan.computeIfAbsent(targetEvent.location(), location -> new Span(targetName, targetEvent.timestamp(), this.currentSpan));  
        }
        
        this.addLatencyOverlayIfNecessary(sourceEvent, targetEvent);
    }
    
    private void addLatencyOverlayIfNecessary(MonitoringEvent startEvent, MonitoringEvent endEvent) {
        var latency = (endEvent.timestamp() - startEvent.timestamp());
        
        if (latency > 0) {
            var latencyOverlay = new LatencyOverlay(startEvent.timestamp(), endEvent.timestamp());
            this.currentSpan.addOverlay(latencyOverlay);
        }
    }

    @Override
    public void onComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection,
            TraceSimulationContext context) {

        this.onComponentChange(exitEvent, returnEvent, exitEvent.name());
    }

    @Override
    public void onUseCaseEndEvent(UseCaseEndEvent event, TraceSimulationContext context) {
        this.rootSpan.endTimestamp(event.timestamp());
    }

}
