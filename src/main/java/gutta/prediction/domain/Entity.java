package gutta.prediction.domain;

import static java.util.Objects.requireNonNull;

public record Entity(String typeName, String id, boolean hasRoot, String rootId) {
    
    public Entity(String typeName, String id) {
        this(typeName, id, false, null);
    }
    
    public Entity(String typeName, String id, String rootId) {
        this(typeName, id, true, requireNonNull(rootId));
    }

}
