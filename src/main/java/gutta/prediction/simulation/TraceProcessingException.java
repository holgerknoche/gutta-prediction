package gutta.prediction.simulation;

import gutta.prediction.event.MonitoringEvent;

/**
 * This exception is thrown if an error occurs during the processing of an event trace.
 */
public class TraceProcessingException extends RuntimeException {

    private static final long serialVersionUID = 7472961941172322418L;

    private final MonitoringEvent offendingEvent;

    private static String formatMessage(MonitoringEvent offendingEvent, String message) {
        return offendingEvent + ": " + message;
    }

    /**
     * Creates a new exception from the given data.
     * 
     * @param offendingEvent The event that caused this exception
     * @param message        The error message
     */
    public TraceProcessingException(MonitoringEvent offendingEvent, String message) {
        super(formatMessage(offendingEvent, message));

        this.offendingEvent = offendingEvent;
    }

    /**
     * Returns the event that caused this exception.
     * 
     * @return see above
     */
    public MonitoringEvent offendingEvent() {
        return this.offendingEvent;
    }

}
