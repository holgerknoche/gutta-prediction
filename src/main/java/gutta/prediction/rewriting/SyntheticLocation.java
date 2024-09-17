package gutta.prediction.rewriting;

import gutta.prediction.event.Location;

/**
 * Implementation of a synthetic location that is inserted as part of the trace rewriting process.
 */
record SyntheticLocation(long id) implements Location {

    @Override
    public boolean isSynthetic() {
        return true;
    }

}
