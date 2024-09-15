package gutta.prediction.domain;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ComponentConnections {

    private final Map<ConnectionKey, ComponentConnection> connectionLookup;

    public ComponentConnections(ComponentConnection... connections) {
        this(Arrays.asList(connections));
    }

    public ComponentConnections(Collection<ComponentConnection> connections) {
        this.connectionLookup = createConnectionLookup(connections);
    }

    private static Map<ConnectionKey, ComponentConnection> createConnectionLookup(Collection<ComponentConnection> connections) {
        var lookup = new HashMap<ConnectionKey, ComponentConnection>(connections.size() * 2);

        for (var connection : connections) {
            lookup.put(new ConnectionKey(connection), connection);

            if (connection.isSymmetric()) {
                lookup.put(new ConnectionKey(connection.target(), connection.source()), connection);
            }
        }

        return lookup;
    }

    public Optional<ComponentConnection> getConnection(Component source, Component target) {
        if (source.equals(target)) {
            return Optional.of(new LocalComponentConnection(source, target, false));
        } else {
            var searchKey = new ConnectionKey(source, target);
            return Optional.ofNullable(this.connectionLookup.get(searchKey));
        }
    }

    private record ConnectionKey(Component source, Component target) {

        public ConnectionKey(ComponentConnection connection) {
            this(connection.source(), connection.target());
        }

    }

}
