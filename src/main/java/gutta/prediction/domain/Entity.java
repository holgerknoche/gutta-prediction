package gutta.prediction.domain;

import static java.util.Objects.requireNonNull;

public record Entity(EntityType type, String id, boolean hasRoot, String rootId) {
    
    public Entity(EntityType type, String id) {
        this(type, id, false, null);
    }
    
    public Entity(EntityType type, String id, String rootId) {
        this(type, id, true, requireNonNull(rootId));
    }

}
