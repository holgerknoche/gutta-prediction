package gutta.prediction.simulation;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ProcessLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionAbortEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for the class {@link TraceSimulatorWorker}.
 */
class TraceSimulatorWorkerTest {
    
    /**
     * Test case: Transaction creation for a trace with explicit transactiond demarcation. 
     */
    @Test
    void explicitTransaction() {
        // Define the individual events
        var traceId = 1234L;
        var location = new ProcessLocation("test", 1234, 1);
        
        // Define the individual events
        var useCaseStartEvent = new UseCaseStartEvent(traceId, 0, location, "uc1");
        var transactionStartEvent = new TransactionStartEvent(traceId, 1, location, "tx1");
        var candidateInvocationEvent = new ServiceCandidateInvocationEvent(traceId, 2, location, "sc1");
        var candidateEntryEvent = new ServiceCandidateEntryEvent(traceId, 3, location, "sc1");
        var entityReadEvent = new EntityReadEvent(traceId, 4, location, "et1", "1");
        var entityWriteEvent = new EntityReadEvent(traceId, 5, location, "et1", "1");
        var candidateExitEvent = new ServiceCandidateExitEvent(traceId, 6, location, "sc1");
        var candidateReturnEvent = new ServiceCandidateReturnEvent(traceId, 7, location, "sc1");
        var transactionCommitEvent = new TransactionCommitEvent(traceId, 8, location, "tx1");
        var useCaseEndEvent = new UseCaseEndEvent(traceId, 9, location, "uc1");
        
        // Build the input trace
        var inputEvents = List.<MonitoringEvent> of(
                useCaseStartEvent,
                transactionStartEvent,
                candidateInvocationEvent,
                candidateEntryEvent,
                entityReadEvent,
                entityWriteEvent,
                candidateExitEvent,
                candidateReturnEvent,
                transactionCommitEvent,
                useCaseEndEvent
                );
        
        // Build the corresponding deployment model
        var component = new Component("c1");        
        var useCase = new UseCase("uc1");        
        var candidate = new ServiceCandidate("sc1", TransactionBehavior.SUPPORTED);

        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component)
                .assignServiceCandidate(candidate, component)
                .build();
        
        // Perform the simulation
        var listener = new StateMonitoringListener();
        var worker = new TraceSimulatorWorker(listener, inputEvents, deploymentModel); 
        worker.processEvents();
                
        // Ensure that the expected states match the actually assumed states
        var transaction = new TopLevelTransaction("tx1", transactionStartEvent, location);
        
        var expectedStates = List.<SimulationState> of(
                new SimulationState(useCaseStartEvent, null, component, location, null),
                new SimulationState(transactionStartEvent, null, component, location, transaction),
                new SimulationState(candidateInvocationEvent, null, component, location, transaction),
                new SimulationState(candidateEntryEvent, candidate, component, location, transaction),
                new SimulationState(entityReadEvent, candidate, component, location, transaction),
                new SimulationState(entityWriteEvent, candidate, component, location, transaction),
                new SimulationState(candidateExitEvent, candidate, component, location, transaction),
                new SimulationState(candidateReturnEvent, null, component, location, transaction),
                new SimulationState(transactionCommitEvent, null, component, location, transaction),
                new SimulationState(useCaseEndEvent, null, component, location, null)
                );
        
        var assumedStates = listener.assumedStates();
        
        assertEquals(expectedStates, assumedStates);
    }
    
    /**
     * Test case: Transaction creation for a trace with implicit transaction demarcation.
     */
    @Test
    void implicitTransaction() {
        // Define the individual events
        var traceId = 1234L;
        var location = new ProcessLocation("test", 1234, 1);
        
        // Define the individual events
        var useCaseStartEvent = new UseCaseStartEvent(traceId, 0, location, "uc1");
        var candidateInvocationEvent = new ServiceCandidateInvocationEvent(traceId, 2, location, "sc1");
        var candidateEntryEvent = new ServiceCandidateEntryEvent(traceId, 3, location, "sc1");
        var entityReadEvent = new EntityReadEvent(traceId, 4, location, "et1", "1");
        var entityWriteEvent = new EntityReadEvent(traceId, 5, location, "et1", "1");
        var candidateExitEvent = new ServiceCandidateExitEvent(traceId, 6, location, "sc1");
        var candidateReturnEvent = new ServiceCandidateReturnEvent(traceId, 7, location, "sc1");
        var useCaseEndEvent = new UseCaseEndEvent(traceId, 9, location, "uc1");
        
        // Build the input trace
        var inputEvents = List.<MonitoringEvent> of(
                useCaseStartEvent,
                candidateInvocationEvent,
                candidateEntryEvent,
                entityReadEvent,
                entityWriteEvent,
                candidateExitEvent,
                candidateReturnEvent,
                useCaseEndEvent
                );
        
        // Build the corresponding deployment model
        var component = new Component("c1");        
        var useCase = new UseCase("uc1");        
        var candidate = new ServiceCandidate("sc1", TransactionBehavior.REQUIRED);

        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component)
                .assignServiceCandidate(candidate, component)
                .build();
        
        // Perform the simulation
        var listener = new StateMonitoringListener();
        var worker = new TraceSimulatorWorker(listener, inputEvents, deploymentModel); 
        worker.processEvents();
                
        // Ensure that the expected states match the actually assumed states
        var transaction = new TopLevelTransaction("synthetic-0", candidateEntryEvent, location);
        
        var expectedStates = List.<SimulationState> of(
                new SimulationState(useCaseStartEvent, null, component, location, null),
                new SimulationState(candidateInvocationEvent, null, component, location, null),
                new SimulationState(candidateEntryEvent, candidate, component, location, transaction),
                new SimulationState(entityReadEvent, candidate, component, location, transaction),
                new SimulationState(entityWriteEvent, candidate, component, location, transaction),
                new SimulationState(candidateExitEvent, candidate, component, location, transaction),
                new SimulationState(candidateReturnEvent, null, component, location, null),
                new SimulationState(useCaseEndEvent, null, component, location, null)
                );
        
        var assumedStates = listener.assumedStates();               
        assertEquals(expectedStates, assumedStates);
    }
    
    /**
     * Test case: An error occurs when a transaction is already active when an explicitly demarcated transaction is started. 
     */
    @Test
    void activeTransactionWhenExplicitTransactionIsStarted() {        
        final var traceId = 1234L;
        final var location = new ProcessLocation("test", 1234, 1);
                         
        // Define the individual events
        var useCaseStartEvent = new UseCaseStartEvent(traceId, 0, location, "uc1");
        var transactionStartEvent = new TransactionStartEvent(traceId, 50, location, "tx1");
        // This event must have explicit transaction demarcation to trigger the error
        var offendingEvent = new TransactionStartEvent(traceId, 100, location, "tx2");
        var useCaseEndEvent = new UseCaseEndEvent(traceId, 200, location, "uc1");
        
        // Build the input trace
        final var inputEvents = List.<MonitoringEvent> of(
                useCaseStartEvent,
                transactionStartEvent,
                offendingEvent,
                useCaseEndEvent
                );
        
        // Build the corresponding deployment model
        var component = new Component("c1");        
        var useCase = new UseCase("uc1");        
        var candidate = new ServiceCandidate("sc1", TransactionBehavior.SUPPORTED);

        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component)
                .assignServiceCandidate(candidate, component)
                .build();
        
        // Perform the simulation
        var worker = new TraceSimulatorWorker(Collections.emptyList(), inputEvents, deploymentModel); 
        var exception = assertThrows(TraceProcessingException.class, () -> worker.processEvents());
        
        // Make sure that the exception has the expected message and occurs at the expected event
        assertEquals(offendingEvent, exception.offendingEvent());
        assertTrue(exception.getMessage().contains("A transaction was active"));
    }
    
    private static class StateMonitoringListener implements TraceSimulationListener {
    
        private final List<SimulationState> assumedStates = new ArrayList<>();
        
        @Override
        public void onEntityReadEvent(EntityReadEvent event, TraceSimulationContext context) {
            this.recordState(event, context);
        }
        
        @Override
        public void onEntityWriteEvent(EntityWriteEvent event, TraceSimulationContext context) {
            this.recordState(event, context);
        }
        
        @Override
        public void onServiceCandidateEntryEvent(ServiceCandidateEntryEvent event, TraceSimulationContext context) {
            this.recordState(event, context);
        }
        
        @Override
        public void onServiceCandidateExitEvent(ServiceCandidateExitEvent event, TraceSimulationContext context) {
            this.recordState(event, context);
        }
        
        @Override
        public void onServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event, TraceSimulationContext context) {
            this.recordState(event, context);
        }
        
        @Override
        public void onServiceCandidateReturnEvent(ServiceCandidateReturnEvent event, TraceSimulationContext context) {
            this.recordState(event, context);
        }
        
        @Override
        public void onTransactionAbortEvent(TransactionAbortEvent event, TraceSimulationContext context) {
            this.recordState(event, context);
        }
        
        @Override
        public void onTransactionCommitEvent(TransactionCommitEvent event, TraceSimulationContext context) {
            this.recordState(event, context);
        }
        
        @Override
        public void onTransactionStartEvent(TransactionStartEvent event, TraceSimulationContext context) {
            this.recordState(event, context);
        }
        
        @Override
        public void onUseCaseEndEvent(UseCaseEndEvent event, TraceSimulationContext context) {
            this.recordState(event, context);
        }
        
        @Override
        public void onUseCaseStartEvent(UseCaseStartEvent event, TraceSimulationContext context) {
            this.recordState(event, context);
        }
        
        private void recordState(MonitoringEvent event, TraceSimulationContext context) {
            var state = new SimulationState(event, context.currentServiceCandidate(), context.currentComponent(), context.currentLocation(), context.currentTransaction());
            this.assumedStates.add(state);
        }
        
        public List<SimulationState> assumedStates() {
            return this.assumedStates;
        }
        
    }
    
    private record SimulationState(MonitoringEvent event, ServiceCandidate candidate, Component component, Location location, Transaction transaction) {}
    
}
