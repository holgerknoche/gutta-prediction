package gutta.prediction.event;

/**
 * A {@link ProcessLocation} represents an observed location within an operating system process.
 */
public record ProcessLocation(String hostname, int processId, long threadId) implements Location {
    
    @Override
    public boolean isSynthetic() {
        return false;
    }

}
