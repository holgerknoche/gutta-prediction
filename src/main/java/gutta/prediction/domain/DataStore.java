package gutta.prediction.domain;

/**
 * A {@link DataStore} represents a persistent data store that stores all {@linkplain Entity entities} of one or more {@linkplain EntityType types}.
 * 
 * @param name The name of this data store
 * @param readWriteConflictBehavior The behavior of this data store in case of a read-write conflict
 */
public record DataStore(String name, ReadWriteConflictBehavior readWriteConflictBehavior) {

}
