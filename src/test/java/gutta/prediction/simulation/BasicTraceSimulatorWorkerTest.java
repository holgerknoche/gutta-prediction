package gutta.prediction.simulation;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionPropagation;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ObservedLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.SyntheticLocation;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for the class {@link BasicTraceSimulatorWorker}.
 */
class BasicTraceSimulatorWorkerTest {
    
    /**
     * Test case: A local transition from a synthetic location keeps the synthetic location.
     */
    @Test
    void localTransitionFromSyntheticLocation() {
        final var traceId = 1234L;
        final var location1 = new ObservedLocation("test", 1234, 1);
        final var location2 = new ObservedLocation("test", 1234, 2);
        
        // Define the individual events
        var useCaseStartEvent = new UseCaseStartEvent(traceId, 0, location1, "uc1");
        var candidateInvocation1Event = new ServiceCandidateInvocationEvent(traceId, 50, location1, "sc1");
        var candidateEntry1Event = new ServiceCandidateEntryEvent(traceId, 50, location2, "sc1");
        var candidateInvocation2Event = new ServiceCandidateInvocationEvent(traceId, 100, location2, "sc2");
        var candidateEntry2Event = new ServiceCandidateEntryEvent(traceId, 100, location2, "sc2");
        var candidateExit2Event = new ServiceCandidateExitEvent(traceId, 150, location2, "sc2");
        var candidateReturn2Event = new ServiceCandidateReturnEvent(traceId, 150, location2, "sc2");
        var candidateExit1Event = new ServiceCandidateExitEvent(traceId, 150, location2, "sc1");
        var candidateReturn1Event = new ServiceCandidateReturnEvent(traceId, 150, location1, "sc1");
        var useCaseEndEvent = new UseCaseEndEvent(traceId, 200, location1, "uc1");
        
        // Build the input trace
        var inputTrace = EventTrace.of(
                useCaseStartEvent,
                candidateInvocation1Event,
                candidateEntry1Event,
                candidateInvocation2Event,
                candidateEntry2Event,
                candidateExit2Event,
                candidateReturn2Event,
                candidateExit1Event,
                candidateReturn1Event,
                useCaseEndEvent
                );        

        // Build the corresponding deployment model
        var useCase = new UseCase("uc1");
        
        var component1 = new Component("c1");
        var component2 = new Component("c2");
        var candidate1 = new ServiceCandidate("sc1");
        var candidate2 = new ServiceCandidate("sc2");

        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component1)
                .assignServiceCandidate(candidate1, component2)
                .assignServiceCandidate(candidate2, component2)
                .build();
        
        // Apply a modification to obtain a synthetic connection
        var modifiedModel = deploymentModel.applyModifications()
                .addSymmetricRemoteConnection(component1, component2, 0, TransactionPropagation.NONE)
                .build();

        // Build a listener to inspect the commit
        var listener = new StateMonitoringListener();
        
        // Perform the simulation
        var worker = new BasicTraceSimulatorWorker(listener, inputTrace, modifiedModel); 
        worker.processEvents();
        
        // Ensure that the expected states match the actually assumed states
        var syntheticLocation = new SyntheticLocation(0);
        
        var expectedStates = List.<SimulationState> of(
                new SimulationState(useCaseStartEvent, null, component1, location1, null),
                new SimulationState(candidateInvocation1Event, null, component1, location1, null),
                new SimulationState(candidateEntry1Event, candidate1, component2, syntheticLocation, null),
                new SimulationState(candidateInvocation2Event, candidate1, component2, syntheticLocation, null),
                new SimulationState(candidateEntry2Event, candidate2, component2, syntheticLocation, null),
                new SimulationState(candidateExit2Event, candidate2, component2, syntheticLocation, null),
                new SimulationState(candidateReturn2Event, candidate1, component2, syntheticLocation, null),
                new SimulationState(candidateExit1Event, candidate1, component2, syntheticLocation, null),
                new SimulationState(candidateReturn1Event, null, component1, location1, null),
                new SimulationState(useCaseEndEvent, null, component1, location1, null)
                );
        
        var assumedStates = listener.assumedStates();
        
        assertEquals(expectedStates, assumedStates);
    }
    
}
