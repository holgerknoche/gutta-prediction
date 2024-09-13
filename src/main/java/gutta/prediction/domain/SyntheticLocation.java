package gutta.prediction.domain;

import gutta.prediction.event.Location;

/**
 * Implementation of a synthetic location that is inserted as part of the trace rewriting process.
 */
class SyntheticLocation implements Location {

    @Override
    public boolean isSynthetic() {
        return true;
    }

}
