package gutta.prediction.event;

/**
 * A {@link ObservedLocation} represents an observed location within an operating system process.
 */
public record ObservedLocation(String hostname, int processId, long threadId) implements Location {
    
    @Override
    public boolean isSynthetic() {
        return false;
    }

}
