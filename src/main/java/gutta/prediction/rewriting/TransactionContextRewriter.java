package gutta.prediction.rewriting;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnections;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;

import java.util.ArrayList;
import java.util.HashMap;
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
    
    static class TransactionContextRewriterWorker extends TraceRewriterWorker {
        
        private Map<Location, Transaction> openTransactions;
        
        private Transaction propagatedTransaction;
        
        public TransactionContextRewriterWorker(List<MonitoringEvent> events, Map<String, Component> useCaseAllocation, Map<String, Component> methodAllocation,
                ComponentConnections connections) {
            
            super(events, useCaseAllocation, methodAllocation, connections);
        }
        
        public List<MonitoringEvent> rewriteTrace() {
            this.rewrittenEvents = new ArrayList<>();
            this.openTransactions = new HashMap<>();
            this.propagatedTransaction = null;

            this.processEvents();

            return this.rewrittenEvents;
        }
        
        @Override
        protected void onServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event) {
            // TODO Auto-generated method stub
            // TODO Determine whether the transaction could be propagated 
        }
        
        @Override
        protected void onServiceCandidateExitEvent(ServiceCandidateExitEvent event) {
            // TODO Auto-generated method stub
            // TODO Commit the current transaction if it is implicitly demarcated and was opened by the appropriate method             
        }                

    }
        
}
