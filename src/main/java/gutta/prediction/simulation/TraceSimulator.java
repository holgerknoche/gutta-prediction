package gutta.prediction.simulation;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link TraceSimulator} provides functionality to simulate the behavior of an event trace on a given deployment model.
 */
public class TraceSimulator {

    private final List<TraceSimulationListener> listeners;

    private final DeploymentModel deploymentModel;

    /**
     * Runs a simulation of the given trace on the given deployment model with the given parameters.
     * 
     * @param trace           The trace to simulate
     * @param deploymentModel The deployment model to simulate the trace on
     * @param mode            The simulation mode to use
     * @param listener        A listener to receive simulation events
     */
    public static void runSimulationOf(EventTrace trace, DeploymentModel deploymentModel, TraceSimulationMode mode, TraceSimulationListener listener) {
        new TraceSimulator(deploymentModel).addListener(listener).processEvents(trace, mode);
    }

    private TraceSimulator(DeploymentModel deploymentModel) {
        this.deploymentModel = deploymentModel;
        this.listeners = new ArrayList<>();
    }

    private TraceSimulator addListener(TraceSimulationListener listener) {
        this.listeners.add(listener);
        return this;
    }

    private void processEvents(EventTrace trace, TraceSimulationMode mode) {
        WorkerCreator workerCreator = switch (mode) {
        case BASIC -> BasicTraceSimulatorWorker::new;
        case WITH_TRANSACTIONS -> TransactionTraceSimulatorWorker::new;
        case WITH_ENTITY_ACCESSES -> EntityAccessSimulatorWorker::new;
        };

        var worker = workerCreator.createWorker(List.copyOf(this.listeners), trace, this.deploymentModel);
        worker.processEvents();
    }

    private interface WorkerCreator {

        TraceSimulatorWorker createWorker(List<TraceSimulationListener> listeners, EventTrace trace, DeploymentModel deploymentModel);

    }

}
