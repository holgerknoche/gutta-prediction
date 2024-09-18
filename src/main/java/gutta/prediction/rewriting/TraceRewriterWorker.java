package gutta.prediction.rewriting;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
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
import gutta.prediction.stream.EventProcessingContext;
import gutta.prediction.stream.EventStreamProcessor;
import gutta.prediction.stream.EventStreamProcessorListener;

import java.util.ArrayList;
import java.util.List;

abstract class TraceRewriterWorker implements EventStreamProcessorListener {
           
    private List<MonitoringEvent> rewrittenEvents;        

    @Override
    public final void onStartOfProcessing() {
        this.rewrittenEvents = new ArrayList<>();
        this.onStartOfRewrite();
    }
    
    @Override
    public final void onEndOfProcessing() {
        this.onEndOfRewrite();
    }
    
    public List<MonitoringEvent> rewriteTrace(List<MonitoringEvent> trace, DeploymentModel deploymentModel) {
        new EventStreamProcessor(deploymentModel)
            .addListener(this)
            .processEvents(trace);
        
        return this.rewrittenEvents;
    }
    
    protected void onStartOfRewrite() {
        // Do nothing by default
    }
    
    protected void onEndOfRewrite() {
     // Do nothing by default
    }
        
    protected void addRewrittenEvent(MonitoringEvent event) {
        this.rewrittenEvents.add(event);
    }
                    
    @Override
    public void onEntityReadEvent(EntityReadEvent event, EventProcessingContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(EntityReadEvent event, EventProcessingContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else {
            this.addRewrittenEvent(new EntityReadEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.entityType(), event.entityIdentifier()));
        }
    }
        
    @Override
    public void onEntityWriteEvent(EntityWriteEvent event, EventProcessingContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(EntityWriteEvent event, EventProcessingContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else {
            this.addRewrittenEvent(new EntityWriteEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.entityType(), event.entityIdentifier()));
        }
    }
        
    @Override    
    public void onServiceCandidateEntryEvent(ServiceCandidateEntryEvent event, EventProcessingContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(ServiceCandidateEntryEvent event, EventProcessingContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else { 
            this.addRewrittenEvent(new ServiceCandidateEntryEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.name()));
        }
    }       
    
    @Override
    public void onServiceCandidateExitEvent(ServiceCandidateExitEvent event, EventProcessingContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(ServiceCandidateExitEvent event, EventProcessingContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else { 
            this.addRewrittenEvent(new ServiceCandidateExitEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.name()));
        }
    }
    
    @Override
    public void onServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event, EventProcessingContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(ServiceCandidateInvocationEvent event, EventProcessingContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else { 
            this.addRewrittenEvent(new ServiceCandidateInvocationEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.name()));
        }
    }            
    
    @Override
    public void onServiceCandidateReturnEvent(ServiceCandidateReturnEvent event, EventProcessingContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(ServiceCandidateReturnEvent event, EventProcessingContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else { 
            this.addRewrittenEvent(new ServiceCandidateReturnEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.name()));
        }
    }
    
    @Override
    public void onTransactionAbortEvent(TransactionAbortEvent event, EventProcessingContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(TransactionAbortEvent event, EventProcessingContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else { 
            this.addRewrittenEvent(new TransactionAbortEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.transactionId(), event.cause()));
        }
    }
        
    @Override
    public void onTransactionCommitEvent(TransactionCommitEvent event, EventProcessingContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(TransactionCommitEvent event, EventProcessingContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else { 
            this.addRewrittenEvent(new TransactionCommitEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.transactionId()));
        }
    }
        
    @Override
    public void onTransactionStartEvent(TransactionStartEvent event, EventProcessingContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(TransactionStartEvent event, EventProcessingContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else { 
            this.addRewrittenEvent(new TransactionStartEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.transactionId(), event.demarcation()));
        }
    }
        
    @Override
    public void onUseCaseStartEvent(UseCaseStartEvent event, EventProcessingContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(UseCaseStartEvent event, EventProcessingContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else { 
            this.addRewrittenEvent(new UseCaseStartEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.name()));
        }
    }
    
    @Override
    public void onUseCaseEndEvent(UseCaseEndEvent event, EventProcessingContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(UseCaseEndEvent event, EventProcessingContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else { 
            this.addRewrittenEvent(new UseCaseEndEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.name()));
        }
    }        

}
