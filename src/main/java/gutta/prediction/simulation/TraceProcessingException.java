package gutta.prediction.simulation;

import gutta.prediction.event.MonitoringEvent;

public class TraceProcessingException extends RuntimeException {

    private static final long serialVersionUID = 7472961941172322418L;
 
    private final MonitoringEvent offendingEvent;
    
    private static String formatMessage(MonitoringEvent offendingEvent, String message) {
        return offendingEvent + ": " + message;
    }
    
    public TraceProcessingException(MonitoringEvent offendingEvent, String message) {
        super(formatMessage(offendingEvent, message));
        
        this.offendingEvent = offendingEvent;
    }
    
    public MonitoringEvent offendingEvent() {
        return this.offendingEvent;
    }
    
}
