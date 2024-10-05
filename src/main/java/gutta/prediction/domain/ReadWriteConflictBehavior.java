package gutta.prediction.domain;

/**
 * Enumeration of possible behavior of a {@link DataStore} in case of a read-write conflict, i.e., when an entity is read that has been changed in another
 * transaction.
 */
public enum ReadWriteConflictBehavior {
    /**
     * Denotes that the data store blocks when an entity is read that has been changed in another transaction. This behavior might occur in a data store that
     * employs read locks.
     */
    BLOCK,
    /**
     * Denotes that the data store returns the state of the entity as it was before the change. This behavior is common in data stores with multi-version
     * concurrency control (MVCC).
     */
    STALE_READ
}
