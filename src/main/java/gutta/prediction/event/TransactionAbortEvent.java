package gutta.prediction.event;

public record TransactionAbortEvent(long traceId, long timestamp, Location location, String transactionId, String cause) implements MonitoringEvent {
	
	@Override
	public <R> R accept(MonitoringEventVisitor<R> visitor) {
		return visitor.handleTransactionAbortEvent(this);
	}
	
}
