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

abstract class TraceRewriterWorker implements TraceSimulationListener {
           
    private RewrittenEventCollector rewrittenEventCollector;

    @Override
    public final void onStartOfProcessing() {
        this.onStartOfRewrite();
    }
    
    @Override
    public final void onEndOfProcessing() {
        this.onEndOfRewrite();
    }
    
    public RewrittenEventTrace rewriteTrace(EventTrace trace, DeploymentModel deploymentModel) {
        if (trace instanceof RewrittenEventTrace rewrittenTrace) {
            this.rewrittenEventCollector = new JoiningRewrittenEventCollector(rewrittenTrace::obtainOriginalEvent);
        } else {
            this.rewrittenEventCollector = new SimpleRewrittenEventCollector();
        }
        
        new TraceSimulator(deploymentModel)
            .addListener(this)
            .processEvents(trace);
        
        return this.rewrittenEventCollector.createTrace();
    }
    
    protected void onStartOfRewrite() {
        // Do nothing by default
    }
    
    protected void onEndOfRewrite() {
     // Do nothing by default
    }
        
    protected void addRewrittenEvent(MonitoringEvent rewrittenEvent, MonitoringEvent originalEvent) {
        this.rewrittenEventCollector.addRewrittenEvent(rewrittenEvent, originalEvent);
    }
                    
    @Override
    public void onEntityReadEvent(EntityReadEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        var rewrittenEvent = this.adjustLocation(event, context);
        this.addRewrittenEvent(rewrittenEvent, event);
    }
    
    protected EntityReadEvent adjustLocation(EntityReadEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            return event;
        } else {
            return new EntityReadEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.entity());
        }
    }
        
    @Override
    public void onEntityWriteEvent(EntityWriteEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        var rewrittenEvent = this.adjustLocation(event, context);
        this.addRewrittenEvent(rewrittenEvent, event);
    }
    
    protected EntityWriteEvent adjustLocation(EntityWriteEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            return event;
        } else {
            return new EntityWriteEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.entity());
        }
    }
        
    @Override    
    public void onServiceCandidateEntryEvent(ServiceCandidateEntryEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        var rewrittenEvent = this.adjustLocation(event, context);
        this.addRewrittenEvent(rewrittenEvent, event);
    }
    
    protected ServiceCandidateEntryEvent adjustLocation(ServiceCandidateEntryEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            return event;
        } else { 
            return new ServiceCandidateEntryEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.name(), event.transactionStarted(), event.transactionId());
        }
    }       
    
    @Override
    public void onServiceCandidateExitEvent(ServiceCandidateExitEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        var rewrittenEvent = this.adjustLocation(event, context);
        this.addRewrittenEvent(rewrittenEvent, event);
    }
    
    protected ServiceCandidateExitEvent adjustLocation(ServiceCandidateExitEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            return event;
        } else { 
            return new ServiceCandidateExitEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.name());
        }
    }
    
    @Override
    public void onServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        var rewrittenEvent = this.adjustLocation(event, context);
        this.addRewrittenEvent(rewrittenEvent, event);
    }
    
    protected ServiceCandidateInvocationEvent adjustLocation(ServiceCandidateInvocationEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            return event;
        } else { 
            return new ServiceCandidateInvocationEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.name());
        }
    }            
    
    @Override
    public void onServiceCandidateReturnEvent(ServiceCandidateReturnEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        var rewrittenEvent = this.adjustLocation(event, context);
        this.addRewrittenEvent(rewrittenEvent, event);
    }
    
    protected ServiceCandidateReturnEvent adjustLocation(ServiceCandidateReturnEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            return event;
        } else { 
            return new ServiceCandidateReturnEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.name());
        }
    }
    
    @Override
    public void onImplicitTransactionAbortEvent(ImplicitTransactionAbortEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        var rewrittenEvent = this.adjustLocation(event, context);
        this.addRewrittenEvent(rewrittenEvent, event);
    }
    
    protected ImplicitTransactionAbortEvent adjustLocation(ImplicitTransactionAbortEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            return event;
        } else { 
            return new ImplicitTransactionAbortEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.transactionId(), event.cause());
        }
    }
    
    @Override
    public void onExplicitTransactionAbortEvent(ExplicitTransactionAbortEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        var rewrittenEvent = this.adjustLocation(event, context);
        this.addRewrittenEvent(rewrittenEvent, event);
    }
    
    protected ExplicitTransactionAbortEvent adjustLocation(ExplicitTransactionAbortEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            return event;
        } else { 
            return new ExplicitTransactionAbortEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.transactionId());
        }
    }
        
    @Override
    public void onTransactionCommitEvent(TransactionCommitEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        var rewrittenEvent = this.adjustLocation(event, context);
        this.addRewrittenEvent(rewrittenEvent, event);
    }
    
    protected TransactionCommitEvent adjustLocation(TransactionCommitEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            return event;
        } else { 
            return new TransactionCommitEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.transactionId());
        }
    }
        
    @Override
    public void onTransactionStartEvent(TransactionStartEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        var rewrittenEvent = this.adjustLocation(event, context);
        this.addRewrittenEvent(rewrittenEvent, event);
    }
    
    protected TransactionStartEvent adjustLocation(TransactionStartEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            return event;
        } else { 
            return new TransactionStartEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.transactionId());
        }
    }
        
    @Override
    public void onUseCaseStartEvent(UseCaseStartEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        var rewrittenEvent = this.adjustLocation(event, context);
        this.addRewrittenEvent(rewrittenEvent, event);
    }
    
    protected UseCaseStartEvent adjustLocation(UseCaseStartEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            return event;
        } else { 
            return new UseCaseStartEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.name());
        }
    }
    
    @Override
    public void onUseCaseEndEvent(UseCaseEndEvent event, TraceSimulationContext context) {
        // By default, copy the event just adjusting the location if necessary
        var rewrittenEvent = this.adjustLocation(event, context);
        this.addRewrittenEvent(rewrittenEvent, event);
    }
    
    protected UseCaseEndEvent adjustLocation(UseCaseEndEvent event, TraceSimulationContext context) {
        if (context.currentLocation().equals(event.location())) {
            return event;
        } else { 
            return new UseCaseEndEvent(event.traceId(), event.timestamp(), context.currentLocation(), event.name());
        }
    }
    
}
