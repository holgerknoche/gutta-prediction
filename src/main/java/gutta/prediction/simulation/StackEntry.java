package gutta.prediction.simulation;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.event.Location;

/**
 * This class represents a stack entry used as part of the trace simulation.
 */
record StackEntry(ServiceCandidate serviceCandidate, Component component, Location location, Transaction transaction) {
    
}
