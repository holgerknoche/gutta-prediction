package gutta.prediction.event;

public record ImplicitTransactionAbortEvent(long traceId, long timestamp, Location location, String transactionId, String cause) implements TransactionEvent {

}
