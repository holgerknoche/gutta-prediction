package gutta.prediction.rewriting;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.stream.EventProcessingContext;

import java.util.List;

public class TransactionContextRewriter implements TraceRewriter {

    private final DeploymentModel deploymentModel;
        
    public TransactionContextRewriter(DeploymentModel deploymentModel) {
        this.deploymentModel = deploymentModel;
    }        
    
    @Override
    public List<MonitoringEvent> rewriteTrace(List<MonitoringEvent> inputTrace) {
        return new TransactionContextRewriterWorker().rewriteTrace(inputTrace, this.deploymentModel);
    }
    
    private static class TransactionContextRewriterWorker extends TraceRewriterWorker {                        
                                
        // TODO Affinities (potentially same transaction on revisit)           
    
        @Override
        public void onServiceCandidateEntryEvent(ServiceCandidateEntryEvent event, EventProcessingContext context) {
            var currentTransaction = context.currentTransaction();
            
            var transactionStartedByCurrentEvent = (currentTransaction != null && event.equals(currentTransaction.startEvent()));
            
            // Rewrite the transaction state if necessary
            ServiceCandidateEntryEvent rewrittenEvent;
            if (!event.transactionStarted() && transactionStartedByCurrentEvent) {
                // If the event originally did not start a transaction, but does now, we add the transaction info
                rewrittenEvent = new ServiceCandidateEntryEvent(event.traceId(), event.timestamp(), event.location(), event.name(), true, currentTransaction.id());                
            } else if (event.transactionStarted() && transactionStartedByCurrentEvent && !currentTransaction.id().equals(event.transactionId())) {
                // If the creation state matches, but the IDs differ, we adjust the ID
                rewrittenEvent = new ServiceCandidateEntryEvent(event.traceId(), event.timestamp(), event.location(), event.name(), true, currentTransaction.id());
            } else if (event.transactionStarted() && !transactionStartedByCurrentEvent) {
                // If the event originally start a transaction, but does not now, we remove the transaction info
                rewrittenEvent = new ServiceCandidateEntryEvent(event.traceId(), event.timestamp(), event.location(), event.name(), false, null);                
            } else {
                // Otherwise, we do not need to rewrite
                rewrittenEvent = event;
            }
            
            // Adjust the location, if necessary
            this.adjustLocationAndAdd(rewrittenEvent, context);
        }
        
    }            
    
}
