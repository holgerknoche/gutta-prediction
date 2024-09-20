package gutta.prediction.rewriting;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ExplicitTransactionAbortEvent;
import gutta.prediction.event.ImplicitTransactionAbortEvent;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import gutta.prediction.simulation.TraceSimulationContext;
import gutta.prediction.simulation.TraceSimulationListener;
import gutta.prediction.simulation.TraceSimulator;

import java.util.ArrayList;
import java.util.List;

abstract class TraceRewriterWorker implements TraceSimulationListener {
           
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
    
    public EventTrace rewriteTrace(EventTrace trace, DeploymentModel deploymentModel) {
        new TraceSimulator(deploymentModel)
            .addListener(this)
            .processEvents(trace);
        
        return EventTrace.of(this.rewrittenEvents);
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
    public void onEntityReadEvent(EntityReadEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(EntityReadEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else {
            this.addRewrittenEvent(new EntityReadEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.entityType(), event.entityIdentifier()));
        }
    }
        
    @Override
    public void onEntityWriteEvent(EntityWriteEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(EntityWriteEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else {
            this.addRewrittenEvent(new EntityWriteEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.entityType(), event.entityIdentifier()));
        }
    }
        
    @Override    
    public void onServiceCandidateEntryEvent(ServiceCandidateEntryEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(ServiceCandidateEntryEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else { 
            this.addRewrittenEvent(new ServiceCandidateEntryEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.name(), event.transactionStarted(), event.transactionId()));
        }
    }       
    
    @Override
    public void onServiceCandidateExitEvent(ServiceCandidateExitEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(ServiceCandidateExitEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else { 
            this.addRewrittenEvent(new ServiceCandidateExitEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.name()));
        }
    }
    
    @Override
    public void onServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(ServiceCandidateInvocationEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else { 
            this.addRewrittenEvent(new ServiceCandidateInvocationEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.name()));
        }
    }            
    
    @Override
    public void onServiceCandidateReturnEvent(ServiceCandidateReturnEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(ServiceCandidateReturnEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else { 
            this.addRewrittenEvent(new ServiceCandidateReturnEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.name()));
        }
    }
    
    @Override
    public void onImplicitTransactionAbortEvent(ImplicitTransactionAbortEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(ImplicitTransactionAbortEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else { 
            this.addRewrittenEvent(new ImplicitTransactionAbortEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.transactionId(), event.cause()));
        }
    }
    
    @Override
    public void onExplicitTransactionAbortEvent(ExplicitTransactionAbortEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(ExplicitTransactionAbortEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else { 
            this.addRewrittenEvent(new ExplicitTransactionAbortEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.transactionId()));
        }
    }
        
    @Override
    public void onTransactionCommitEvent(TransactionCommitEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(TransactionCommitEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else { 
            this.addRewrittenEvent(new TransactionCommitEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.transactionId()));
        }
    }
        
    @Override
    public void onTransactionStartEvent(TransactionStartEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(TransactionStartEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else { 
            this.addRewrittenEvent(new TransactionStartEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.transactionId()));
        }
    }
        
    @Override
    public void onUseCaseStartEvent(UseCaseStartEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(UseCaseStartEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else { 
            this.addRewrittenEvent(new UseCaseStartEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.name()));
        }
    }
    
    @Override
    public void onUseCaseEndEvent(UseCaseEndEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        this.adjustLocationAndAdd(event, context);
    }
    
    protected void adjustLocationAndAdd(UseCaseEndEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            this.addRewrittenEvent(event);
        } else { 
            this.addRewrittenEvent(new UseCaseEndEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.name()));
        }
    }        

}
