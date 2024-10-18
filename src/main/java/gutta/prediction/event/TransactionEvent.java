package gutta.prediction.event;

public sealed interface TransactionEvent extends MonitoringEvent
        permits ExplicitTransactionAbortEvent, ImplicitTransactionAbortEvent, TransactionStartEvent, TransactionCommitEvent {

    String transactionId();

}
