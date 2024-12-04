package gutta.prediction.simulation;

/**
 * Enumeration of all possible trace simulation modes.
 */
public enum TraceSimulationMode {
    
    /**
     * Traverse all events without simulating transactions or entity accesses. 
     */
    BASIC,
    
    /**
     * Traverse all events and simulate transactions, but do not track entity accesses.
     */
    WITH_TRANSACTIONS,
    
    /**
     * Traverse all entities, simulating transactions and keeping track of entity accesses.
     */
    WITH_ENTITY_ACCESSES    
}
