package gutta.prediction.analysis.latency;

import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import gutta.prediction.simulation.TraceSimulationContext;
import gutta.prediction.simulation.TraceSimulationListener;
import gutta.prediction.simulation.TraceSimulator;

class LatencyAnalyzer implements TraceSimulationListener {
    
    private long startTime = 0;
    
    private long endTime = 0;
    
    private long totalLatency = 0;
    
    public Result analyzeTrace(EventTrace trace, DeploymentModel deploymentModel) {
        new TraceSimulator(deploymentModel)
            .addListener(this)
            .processEvents(trace);
        
        var duration = (this.endTime - this.startTime);
        var latencyPercentage = (duration == 0) ? 0 : (float) (this.totalLatency) / (float) duration; 
        
        return new Result(duration, this.totalLatency, latencyPercentage);
    }
    
    @Override
    public void onUseCaseStartEvent(UseCaseStartEvent event, TraceSimulationContext context) {
        this.startTime = event.timestamp();
    }
    
    @Override
    public void onComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ComponentConnection connection, TraceSimulationContext context) {
        this.registerLatency(invocationEvent, entryEvent);
    }
    
    @Override
    public void onComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection, TraceSimulationContext context) {
        this.registerLatency(exitEvent, returnEvent);
    }
    
    private void registerLatency(MonitoringEvent startEvent, MonitoringEvent endEvent) {
        long latency = (endEvent.timestamp() - startEvent.timestamp());
        this.totalLatency += latency;
    }
    
    @Override
    public void onUseCaseEndEvent(UseCaseEndEvent event, TraceSimulationContext context) {
        this.endTime = event.timestamp();
    }
    
    record Result(long duration, long totalLatency, float latencyPercentage) {}

}
