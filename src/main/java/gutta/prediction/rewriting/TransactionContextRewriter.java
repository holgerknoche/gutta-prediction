package gutta.prediction.rewriting;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.ComponentConnections;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionContextRewriter implements TraceRewriter {

    private final List<ServiceCandidate> serviceCandidates;
    
    private final Map<String, Component> useCaseAllocation;
    
    private final Map<ServiceCandidate, Component> candidateAllocation;
    
    private final ComponentConnections connections;
    
    public TransactionContextRewriter(List<ServiceCandidate> serviceCandidates, Map<String, Component> useCaseAllocation, Map<ServiceCandidate, Component> candidateAllocation, ComponentConnections connections) {
        this.serviceCandidates = serviceCandidates;
        this.useCaseAllocation = useCaseAllocation;
        this.candidateAllocation = candidateAllocation;
        this.connections = connections;
    }        
    
    @Override
    public List<MonitoringEvent> rewriteTrace(List<MonitoringEvent> inputTrace) {
        return new TransactionContextRewriterWorker(inputTrace, this.serviceCandidates, this.useCaseAllocation, this.candidateAllocation, this.connections).rewriteTrace();
    }
    
    static class TransactionContextRewriterWorker extends TraceRewriterWorker {                        
        
        private Map<Location, Transaction> openTransactions;
        
        private Transaction currentTransaction;                
        
        public TransactionContextRewriterWorker(List<MonitoringEvent> events, List<ServiceCandidate> serviceCandidates, Map<String, Component> useCaseAllocation, Map<ServiceCandidate, Component> candidateAllocation,
                ComponentConnections connections) {
            
            super(events, serviceCandidates, useCaseAllocation, candidateAllocation, connections);
        }
                
        @Override
        protected void onStartOfRewrite() {
            this.openTransactions = new HashMap<>();
            this.currentTransaction = null;
        }
        
    }
        
}
