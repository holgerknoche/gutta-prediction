package gutta.prediction.domain;

import static java.util.Objects.requireNonNull;

/**
 * An {@link Entity} represents an actual data entity in some data store.
 * 
 * @param typeName The name of the entity's type
 * @param id       The entity's ID (e.g., its primary key)
 * @param hasRoot  Denotes whether this entity is part of an aggregate
 * @param rootId   The ID of the aggregate root this entity belongs to (only applicable if {@link #hasRoot} is set)
 */
public record Entity(String typeName, String id, boolean hasRoot, String rootId) {

    /**
     * Creates a new entity from the given data that is not part of an aggregate.
     * 
     * @param typeName The name of the entity's type
     * @param id       The entity's ID (e.g., its primary key)
     */
    public Entity(String typeName, String id) {
        this(typeName, id, false, null);
    }

    /**
     * Creates a new entity from the given data that is part of an aggregate with the given root entity.
     * 
     * @param typeName   The name of the entity's type
     * @param id         The entity's ID (e.g., its primary key)
     * @param rootEntity The root entity of the aggegate
     */
    public Entity(String typeName, String id, Entity rootEntity) {
        this(typeName, id, requireNonNull(rootEntity).id());
    }

    /**
     * Creates a new entity from the given data that is part of an aggregate with the given root ID.
     * 
     * @param typeName The name of the entity's type
     * @param id       The entity's ID (e.g., its primary key)
     * @param rootId   The root entity of the aggegate
     */
    public Entity(String typeName, String id, String rootId) {
        this(typeName, id, true, requireNonNull(rootId));
    }

}
