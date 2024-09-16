package gutta.prediction.rewriting;

import gutta.prediction.event.MonitoringEvent;

class TraceRewriteException extends RuntimeException {        
    
    private static final long serialVersionUID = 8650497556688075464L;

    private final MonitoringEvent offendingEvent;
    
    private static String formatMessage(MonitoringEvent offendingEvent, String message) {
        return offendingEvent + ": " + message;
    }
    
    public TraceRewriteException(MonitoringEvent offendingEvent, String message) {
        super(formatMessage(offendingEvent, message));
        
        this.offendingEvent = offendingEvent;
    }
    
    public MonitoringEvent offendingEvent() {
        return this.offendingEvent;
    }
    
}
