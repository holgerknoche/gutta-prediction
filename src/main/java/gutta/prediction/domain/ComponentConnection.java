package gutta.prediction.domain;

import gutta.prediction.domain.ComponentConnectionProperties.ConnectionType;

public record ComponentConnection(Component source, Component target, boolean symmetric, ComponentConnectionProperties properties) {
	
	public ComponentConnection(Component source, Component target, boolean symmetric, long latency, ConnectionType type, boolean modified) {
		this(source, target, symmetric, new ComponentConnectionProperties(latency, type, modified));
	}

}
