package gutta.prediction.domain;

public record ComponentConnectionProperties(long latency, ConnectionType type, boolean modified) {

	public enum ConnectionType {
		LOCAL,
		REMOTE_WITH_TRANSACTION_PROPAGATION,
		REMOTE_WITHOUT_TRANSACTION_PROPAGATION
	}
	
}
