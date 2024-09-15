package gutta.prediction.rewriting;

import gutta.prediction.common.AbstractMonitoringEventProcessor;
import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnections;
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

import java.util.List;
import java.util.Map;

class TraceRewriterWorker extends AbstractMonitoringEventProcessor {

    protected List<MonitoringEvent> rewrittenEvents;

    protected TraceRewriterWorker(List<MonitoringEvent> events, Map<String, Component> useCaseAllocation, Map<String, Component> methodAllocation,
            ComponentConnections connections) {

        super(events, useCaseAllocation, methodAllocation, connections);
    }
    
    protected Void addRewrittenEvent(MonitoringEvent event) {
        this.rewrittenEvents.add(event);
        return null;
    }
    
    protected Void copyUnchanged(MonitoringEvent event) {
        return this.addRewrittenEvent(event);
    }
    
    @Override
    public Void handleEntityReadEvent(EntityReadEvent event) {
        // By default, copy the event without making any changes 
        return this.copyUnchanged(event);
    }
    
    @Override
    public Void handleEntityWriteEvent(EntityWriteEvent event) {
        // By default, copy the event without making any changes 
        return this.copyUnchanged(event);
    }
    
    @Override
    public Void handleServiceCandidateEntryEvent(ServiceCandidateEntryEvent event) {
        // By default, copy the event without making any changes 
        return this.copyUnchanged(event);
    }
    
    @Override
    public Void handleServiceCandidateExitEvent(ServiceCandidateExitEvent event) {
        // By default, copy the event without making any changes 
        return this.copyUnchanged(event);
    }
    
    @Override
    public Void handleServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event) {
        // By default, copy the event without making any changes 
        return this.copyUnchanged(event);
    }
    
    @Override
    public Void handleServiceCandidateReturnEvent(ServiceCandidateReturnEvent event) {
        // By default, copy the event without making any changes 
        return this.copyUnchanged(event);
    }
    
    @Override
    public Void handleTransactionAbortEvent(TransactionAbortEvent event) {
        // By default, copy the event without making any changes 
        return this.copyUnchanged(event);
    }
    
    @Override
    public Void handleTransactionCommitEvent(TransactionCommitEvent event) {
        // By default, copy the event without making any changes 
        return this.copyUnchanged(event);
    }
    
    @Override
    public Void handleTransactionStartEvent(TransactionStartEvent event) {
        // By default, copy the event without making any changes 
        return this.copyUnchanged(event);
    }
    
    @Override
    public Void handleUseCaseStartEvent(UseCaseStartEvent event) {
     // By default, copy the event without making any changes 
        return this.copyUnchanged(event);
    }
    
    @Override
    public Void handleUseCaseEndEvent(UseCaseEndEvent event) {
     // By default, copy the event without making any changes 
        return this.copyUnchanged(event);
    }

}
