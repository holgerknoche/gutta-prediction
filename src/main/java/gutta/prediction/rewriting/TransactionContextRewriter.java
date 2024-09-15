package gutta.prediction.rewriting;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnections;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransactionContextRewriter implements TraceRewriter {

    private final Map<String, Component> useCaseAllocation;
    
    private final Map<String, Component> methodAllocation;
    
    private final ComponentConnections connections;
    
    public TransactionContextRewriter(Map<String, Component> useCaseAllocation, Map<String, Component> methodAllocation, ComponentConnections connections) {
        this.useCaseAllocation = useCaseAllocation;
        this.methodAllocation = methodAllocation;
        this.connections = connections;
    }        
    
    @Override
    public List<MonitoringEvent> rewriteTrace(List<MonitoringEvent> inputTrace) {
        return new TransactionContextRewriterWorker(inputTrace, useCaseAllocation, methodAllocation, connections).rewriteTrace();
    }
    
    private static class TransactionContextRewriterWorker extends TraceRewriterWorker {
        
        public TransactionContextRewriterWorker(List<MonitoringEvent> events, Map<String, Component> useCaseAllocation, Map<String, Component> methodAllocation,
                ComponentConnections connections) {
            
            super(events, useCaseAllocation, methodAllocation, connections);
        }
        
        public List<MonitoringEvent> rewriteTrace() {
            this.rewrittenEvents = new ArrayList<>();

            this.processEvents();

            return this.rewrittenEvents;
        }
        
        @Override
        public Void handleServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event) {
            // TODO Auto-generated method stub
            return super.handleServiceCandidateInvocationEvent(event);
        }
        
        @Override
        public Void handleServiceCandidateExitEvent(ServiceCandidateExitEvent event) {
            // TODO Auto-generated method stub
            return super.handleServiceCandidateExitEvent(event);
        }
                
    }
    
}
