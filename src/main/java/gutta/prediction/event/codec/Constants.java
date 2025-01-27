package gutta.prediction.event.codec;

/**
 * This class specifies constants for the event trace storage format.
 */
class Constants {

    /**
     * Event type marker for a use case start event.
     */
    public static final byte EVENT_TYPE_USE_CASE_START = 0x01;

    /**
     * Event type marker for a use case end event.
     */
    public static final byte EVENT_TYPE_USE_CASE_END = 0x02;

    /**
     * Event type marker for a entity read event.
     */
    public static final byte EVENT_TYPE_ENTITY_READ = 0x03;

    /**
     * Event type marker for a entity write event.
     */
    public static final byte EVENT_TYPE_ENTITY_WRITE = 0x04;

    /**
     * Event type marker for a transaction start event.
     */
    public static final byte EVENT_TYPE_TRANSACTION_START = 0x05;

    /**
     * Event type marker for a transaction commit event.
     */
    public static final byte EVENT_TYPE_TRANSACTION_COMMIT = 0x06;

    /**
     * Event type marker for an explicit transaction abort event.
     */
    public static final byte EVENT_TYPE_EXPLICIT_TRANSACTION_ABORT = 0x07;

    /**
     * Event type marker for an implicit transaction abort event.
     */
    public static final byte EVENT_TYPE_IMPLICIT_TRANSACTION_ABORT = 0x08;

    /**
     * Event type marker for a service candidate invocation event.
     */
    public static final byte EVENT_TYPE_SERVICE_CANDIDATE_INVOCATION = 0x09;

    /**
     * Event type marker for a service candidate entry event.
     */
    public static final byte EVENT_TYPE_SERVICE_CANDIDATE_ENTRY = 0x0A;

    /**
     * Event type marker for a service candidate exit event.
     */
    public static final byte EVENT_TYPE_SERVICE_CANDIDATE_EXIT = 0x0B;

    /**
     * Event type marker for a service candidate return event.
     */
    public static final byte EVENT_TYPE_SERVICE_CANDIDATE_RETURN = 0x0C;

    /**
     * Location type marker for an observed location.
     */
    public static final byte LOCATION_TYPE_OBSERVED = 0x01;

    /**
     * Location type marker for a synthetic location.
     */
    public static final byte LOCATION_TYPE_SYNTHETIC = 0x02;

}
