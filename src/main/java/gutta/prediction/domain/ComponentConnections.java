package gutta.prediction.domain;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import gutta.prediction.domain.ComponentConnectionProperties.ConnectionType;

public class ComponentConnections {		
		
	private static final ComponentConnectionProperties IDENTITY_CONNECTION_PROPERTIES = new ComponentConnectionProperties(0, ConnectionType.LOCAL, false);
	
	private final Map<ConnectionKey, ComponentConnectionProperties> connectionPropertiesLookup;
	
	public ComponentConnections(ComponentConnection... connections) {
		this(Arrays.asList(connections));
	}
	
	public ComponentConnections(Collection<ComponentConnection> connections) {
		this.connectionPropertiesLookup = createPropertiesLookup(connections);
	}
	
	private static Map<ConnectionKey, ComponentConnectionProperties> createPropertiesLookup(Collection<ComponentConnection> connections) {
		var lookup = new HashMap<ConnectionKey, ComponentConnectionProperties>(connections.size() * 2);
		
		for (var connection : connections) {
			lookup.put(new ConnectionKey(connection), connection.properties());
			
			if (connection.symmetric()) {
				lookup.put(new ConnectionKey(connection.target(), connection.source()), connection.properties());
			}
		}
		
		return lookup;
	}
	
	public Optional<ComponentConnectionProperties> getConnection(Component source, Component target) {
		if (source.equals(target)) {
			return Optional.of(IDENTITY_CONNECTION_PROPERTIES);
		} else {
			var searchKey = new ConnectionKey(source, target);
			return Optional.ofNullable(this.connectionPropertiesLookup.get(searchKey));
		}		
	}
	
	private record ConnectionKey(Component source, Component target) {
		
		public ConnectionKey(ComponentConnection connection) {
			this(connection.source(), connection.target());
		}
		
	}

}
