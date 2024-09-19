package gutta.prediction.simulation;

import gutta.prediction.event.Location;

/**
 * Implementation of a synthetic location that is inserted as part of the trace rewriting process.
 */
public record SyntheticLocation(long id) implements Location {

    @Override
    public boolean isSynthetic() {
        return true;
    }

}
