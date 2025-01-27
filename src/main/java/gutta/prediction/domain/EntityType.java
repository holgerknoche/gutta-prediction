package gutta.prediction.domain;

/**
 * An {@link EntityType} represents the type of {@linkplain Entity entities} in a {@linkplain DeploymentModel deployment model}.
 * 
 * @param name     The entity type's name
 * @param rootType The entity type's root type if it is part of an aggregate
 */
public record EntityType(String name, EntityType rootType) {

    /**
     * Creates an entity type that is not part of an aggregate.
     * 
     * @param name The entity type's name
     */
    public EntityType(String name) {
        this(name, null);
    }

}
