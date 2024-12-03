package gutta.prediction.event;

/**
 * A {@link ObservedLocation} represents a location that was actually observed within an operating system process.
 */
public record ObservedLocation(String hostname, int processId, long threadId) implements Location {
    
    @Override
    public boolean isSynthetic() {
        return false;
    }

}
