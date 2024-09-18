package gutta.prediction.stream;

import gutta.prediction.event.MonitoringEvent;

public class TraceProcessingException extends RuntimeException {

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
