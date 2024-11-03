package gutta.prediction.domain;

public record Entity(EntityType type, String id, boolean hasRoot, String rootId) {
    
    public Entity(EntityType type, String id) {
        this(type, id, false, null);
    }

}
