package gutta.prediction.event;

public class MonitoringEventVisitor {
    
    protected void handleMonitoringEvent(MonitoringEvent event) {
        // Dispatch to the specific handler methods
        switch (event) {
        case EntityReadEvent specificEvent -> this.handleEntityReadEvent(specificEvent);
        case EntityWriteEvent specificEvent -> this.handleEntityWriteEvent(specificEvent);
        case ExplicitTransactionAbortEvent specificEvent -> this.handleExplicitTransactionAbortEvent(specificEvent); 
        case ImplicitTransactionAbortEvent specificEvent -> this.handleImplicitTransactionAbortEvent(specificEvent);
        case ServiceCandidateEntryEvent specificEvent -> this.handleServiceCandidateEntryEvent(specificEvent);
        case ServiceCandidateExitEvent specificEvent -> this.handleServiceCandidateExitEvent(specificEvent);
        case ServiceCandidateInvocationEvent specificEvent -> this.handleServiceCandidateInvocationEvent(specificEvent);
        case ServiceCandidateReturnEvent specificEvent -> this.handleServiceCandidateReturnEvent(specificEvent);
        case TransactionCommitEvent specificEvent -> this.handleTransactionCommitEvent(specificEvent);
        case TransactionStartEvent specificEvent -> this.handleTransactionStartEvent(specificEvent);
        case UseCaseEndEvent specificEvent -> this.handleUseCaseEndEvent(specificEvent);
        case UseCaseStartEvent specificEvent -> this.handleUseCaseStartEvent(specificEvent);
        }
    }

    protected void handleEntityReadEvent(EntityReadEvent event) {
        // Do nothing by default
    }

    protected void handleEntityWriteEvent(EntityWriteEvent event) {
        // Do nothing by default
    }

    protected void handleServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event) {
        // Do nothing by default
    }

    protected void handleServiceCandidateEntryEvent(ServiceCandidateEntryEvent event) {
        // Do nothing by default
    }

    protected void handleServiceCandidateExitEvent(ServiceCandidateExitEvent event) {
        // Do nothing by default
    }

    protected void handleServiceCandidateReturnEvent(ServiceCandidateReturnEvent event) {
        // Do nothing by default
    }

    protected void handleImplicitTransactionAbortEvent(ImplicitTransactionAbortEvent event) {
        // Do nothing by default
    }

    protected void handleExplicitTransactionAbortEvent(ExplicitTransactionAbortEvent event) {
        // Do nothing by default
    }

    protected void handleTransactionCommitEvent(TransactionCommitEvent event) {
        // Do nothing by default
    }

    protected void handleTransactionStartEvent(TransactionStartEvent event) {
        // Do nothing by default
    }

    protected void handleUseCaseEndEvent(UseCaseEndEvent event) {
        // Do nothing by default
    }

    protected void handleUseCaseStartEvent(UseCaseStartEvent event) {
        // Do nothing by default
    }

}
