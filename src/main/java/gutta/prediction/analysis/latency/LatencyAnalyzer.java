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
import gutta.prediction.simulation.TraceSimulationMode;

import java.util.ArrayDeque;
import java.util.Deque;

import static gutta.prediction.simulation.TraceSimulator.runSimulationOf;

class LatencyAnalyzer implements TraceSimulationListener {
    
    private long startTime = 0;
    
    private long endTime = 0;
    
    private long totalLatency = 0;
    
    private long totalTimeInAsyncInvocations = 0;
    
    private Deque<ServiceCandidateInvocationEvent> asyncStack = new ArrayDeque<>();
    
    public Result analyzeTrace(EventTrace trace, DeploymentModel deploymentModel) {
        runSimulationOf(trace, deploymentModel, TraceSimulationMode.BASIC, this);
        
        var duration = (this.endTime - this.startTime) - this.totalTimeInAsyncInvocations;
        var latencyPercentage = (duration == 0) ? 0 : (float) (this.totalLatency) / (float) duration; 
        
        return new Result(duration, this.totalLatency, latencyPercentage);
    }
    
    @Override
    public void onUseCaseStartEvent(UseCaseStartEvent event, TraceSimulationContext context) {
        this.startTime = event.timestamp();
    }
    
    @Override
    public void afterComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent, ComponentConnection connection, TraceSimulationContext context) {
        var serviceCandidate = context.currentServiceCandidate();
        
        if (serviceCandidate.asynchronous()) {
            // If the invocation is asynchronous, push the invocation event so that the time spent in the invocation
            // can be calculated on return
            this.asyncStack.push(invocationEvent);
        } else {
            // Otherwise, simply register the latency
            this.registerLatency(invocationEvent, entryEvent);
        }
    }
    
    @Override
    public void beforeComponentReturn(ServiceCandidateExitEvent exitEvent, ServiceCandidateReturnEvent returnEvent, ComponentConnection connection, TraceSimulationContext context) {
        var serviceCandidate = context.currentServiceCandidate();
        
        if (serviceCandidate.asynchronous()) {
            // If the invocation was asynchronous, pop the corresponding invocation event to calculate the time spent
            // in the invocation
            var invocationEvent = this.asyncStack.pop();
            var timeSpentInAsyncInvocation = (returnEvent.timestamp() - invocationEvent.timestamp());
            this.totalTimeInAsyncInvocations += timeSpentInAsyncInvocation;
        } else {
            // Otherwise, simply register the latency
            this.registerLatency(exitEvent, returnEvent);
        }
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
