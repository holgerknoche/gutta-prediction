package gutta.prediction.event;

public interface MonitoringEventVisitor<R> {
	
	default R handleEntityReadEvent(EntityReadEvent event) {
		return null;
	}
	
	default R handleEntityWriteEvent(EntityWriteEvent event) {
		return null;
	}
	
	default R handleServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event) {
		return null;
	}
	
	default R handleServiceCandidateEntryEvent(ServiceCandidateEntryEvent event) {
		return null;
	}
	
	default R handleServiceCandidateExitEvent(ServiceCandidateExitEvent event) {
		return null;
	}
	
	default R handleServiceCandidateReturnEvent(ServiceCandidateReturnEvent event) {
		return null;
	}
	
	default R handleTransactionAbortEvent(TransactionAbortEvent event) {
		return null;
	}
	
	default R handleTransactionCommitEvent(TransactionCommitEvent event) {
		return null;
	}
	
	default R handleTransactionStartEvent(TransactionStartEvent event) {
		return null;
	}
	
	default R handleUseCaseEndEvent(UseCaseEndEvent event) {
		return null;
	}
	
	default R handleUseCaseStartEvent(UseCaseStartEvent event) {
		return null;
	}

}
