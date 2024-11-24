package gutta.prediction.ui.analysis;

import gutta.prediction.domain.ComponentConnection;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.simulation.TraceSimulationContext;
import gutta.prediction.simulation.TraceSimulationListener;
import gutta.prediction.simulation.TraceSimulationMode;
import gutta.prediction.simulation.TraceSimulator;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link RemoteCallsLister} searches a trace for remote calls and returns information on them.
 */
public class RemoteCallsLister {

    /**
     * Searches the given trace for remote calls.
     * 
     * @param trace           The trace to analyze
     * @param deploymentModel The deployment model to use for the analysis
     * @return The (possibly empty) list of found remote calls
     */
    public List<RemoteCall> listRemoteCalls(EventTrace trace, DeploymentModel deploymentModel) {
        return new RemoteCallsListerWorker(deploymentModel).findCalls(trace);
    }

    private static class RemoteCallsListerWorker implements TraceSimulationListener {

        private final DeploymentModel deploymentModel;

        private final List<RemoteCall> calls = new ArrayList<>();

        public RemoteCallsListerWorker(DeploymentModel deploymentModel) {
            this.deploymentModel = deploymentModel;
        }

        public List<RemoteCall> findCalls(EventTrace trace) {
            TraceSimulator.runSimulationOf(trace, this.deploymentModel, TraceSimulationMode.BASIC, this);
            return this.calls;
        }

        @Override
        public void beforeComponentTransition(ServiceCandidateInvocationEvent invocationEvent, ServiceCandidateEntryEvent entryEvent,
                ComponentConnection connection, TraceSimulationContext context) {
            
            if (connection.isRemote()) {
                var serviceCandidate = this.deploymentModel.resolveServiceCandidateByName(invocationEvent.name()).orElseThrow();
                var call = new RemoteCall(invocationEvent.timestamp(), connection.source(), connection.target(), serviceCandidate, connection);
                this.calls.add(call);
            }
        }

    }

}
