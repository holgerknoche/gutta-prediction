package gutta.prediction.analysis.consistency;

/**
 * This enumeration lists the options to enable or disable interleaving entity writes as part of the {@link ConsistencyIssuesAnalysis}.
 */
public enum CheckInterleavingAccesses {

    /**
     * Denotes that checks for interleaving entity writes should be performed.
     */
    YES,

    /**
     * Denotes that checks for interleaving entity writes should not be performed.
     */
    NO

}
