package gutta.prediction.event.codec;

class Constants {
    
    public static final byte EVENT_TYPE_USE_CASE_START = 1;
    
    public static final byte EVENT_TYPE_USE_CASE_END = 2;
    
    public static final byte EVENT_TYPE_ENTITY_READ = 3;
    
    public static final byte EVENT_TYPE_ENTITY_WRITE = 4;
    
    public static final byte EVENT_TYPE_TRANSACTION_START = 5;
    
    public static final byte EVENT_TYPE_TRANSACTION_COMMIT = 6;
    
    public static final byte EVENT_TYPE_EXPLICIT_TRANSACTION_ABORT = 7;
    
    public static final byte EVENT_TYPE_IMPLICIT_TRANSACTION_ABORT = 8;
    
    public static final byte LOCATION_TYPE_OBSERVED = 1;
    
    public static final byte LOCATION_TYPE_SYNTHETIC = 2;
    
}
