package gutta.prediction.simulation;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;

record SimulationState(MonitoringEvent event, ServiceCandidate candidate, Component component, Location location, Transaction transaction) {}
