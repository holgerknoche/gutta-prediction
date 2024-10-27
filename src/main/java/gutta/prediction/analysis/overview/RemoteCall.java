package gutta.prediction.analysis.overview;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.ServiceCandidate;

public record RemoteCall(long timestamp, Component sourceComponent, Component targetComponent, ServiceCandidate serviceCanidate, ComponentConnection connection) {

}
