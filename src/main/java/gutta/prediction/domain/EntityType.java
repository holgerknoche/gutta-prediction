package gutta.prediction.domain;

public record EntityType(String name, EntityType rootType) {
    
    public EntityType(String name) {
        this(name, null);
    }

}
