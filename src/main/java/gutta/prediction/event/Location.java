package gutta.prediction.event;

/**
 * A {@link Location} represents the actual location where a component is executed, such as an operating system process on a specific machine. With respect to
 * our approach, components share local transactions within a location, and cannot share them across locations.
 */
public interface Location {
    
    boolean isArtificial();
    
}
