package gutta.prediction.simulation;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.event.Location;

record StackEntry(ServiceCandidate serviceCandidate, Component component, Location location, Transaction transaction) {
    
}
