package gutta.prediction.ui.analysis;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.ServiceCandidate;

/**
 * A {@link RemoteCall} provides information about a remote call between components.
 * 
 * @param timestamp        The timestamp at which this call occurred in the trace
 * @param sourceComponent  The component from which the call originated
 * @param targetComponent  The component in which the invoked service resides
 * @param serviceCandidate The invoked service candidate
 * @param connection       The connection over which the invocation occurred
 * 
 */
public record RemoteCall(long timestamp, Component sourceComponent, Component targetComponent, ServiceCandidate serviceCanidate,
        ComponentConnection connection) {

}
