package gutta.prediction.event.codec;

class Constants {
    
    public static final byte EVENT_TYPE_USE_CASE_START = 0x01;
    
    public static final byte EVENT_TYPE_USE_CASE_END = 0x02;
    
    public static final byte EVENT_TYPE_ENTITY_READ = 0x03;
    
    public static final byte EVENT_TYPE_ENTITY_WRITE = 0x04;
    
    public static final byte EVENT_TYPE_TRANSACTION_START = 0x05;
    
    public static final byte EVENT_TYPE_TRANSACTION_COMMIT = 0x06;
    
    public static final byte EVENT_TYPE_EXPLICIT_TRANSACTION_ABORT = 0x07;
    
    public static final byte EVENT_TYPE_IMPLICIT_TRANSACTION_ABORT = 0x08;
    
    public static final byte EVENT_TYPE_SERVICE_CANDIDATE_INVOCATION = 0x09;
    
    public static final byte EVENT_TYPE_SERVICE_CANDIDATE_ENTRY = 0x0A;
    
    public static final byte EVENT_TYPE_SERVICE_CANDIDATE_EXIT = 0x0B;
    
    public static final byte EVENT_TYPE_SERVICE_CANDIDATE_RETURN = 0x0C;
    
    public static final byte LOCATION_TYPE_OBSERVED = 0x01;
    
    public static final byte LOCATION_TYPE_SYNTHETIC = 0x02;
    
}
