package gutta.prediction.simulation;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.TransactionPropagation;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.ExplicitTransactionAbortEvent;
import gutta.prediction.event.ImplicitTransactionAbortEvent;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ProcessLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import gutta.prediction.simulation.Transaction.Demarcation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

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
        var transaction = new TopLevelTransaction("tx1", transactionStartEvent, location, Demarcation.EXPLICIT);
        
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
        var transaction = new TopLevelTransaction("synthetic-0", candidateEntryEvent, location, Demarcation.IMPLICIT);
        
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
    
    static Stream<Arguments> activeTransactionArguments() {
        return Stream.of(
                arguments(TransactionBehavior.MANDATORY, ExpectedOutcome.SAME_TRANSACTION, null),
                arguments(TransactionBehavior.NEVER, ExpectedOutcome.ERROR, "Active transaction found for"),
                arguments(TransactionBehavior.NOT_SUPPORTED, ExpectedOutcome.NO_TRANSACTION, null),
                arguments(TransactionBehavior.REQUIRED, ExpectedOutcome.SAME_TRANSACTION, null),
                arguments(TransactionBehavior.REQUIRES_NEW, ExpectedOutcome.NEW_TRANSACTION, null),
                arguments(TransactionBehavior.SUPPORTED, ExpectedOutcome.SAME_TRANSACTION, null)
                );
    }
        
    /**
     * Test cases: Different transaction behaviors (see {@link TransactionBehavior}) when a transaction is active. 
     */
    @ParameterizedTest
    @MethodSource("activeTransactionArguments")
    void behaviorWithActiveTransaction(TransactionBehavior behavior, ExpectedOutcome expectedOutcome, String expectedErrorMessageFragment) {
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
        var candidate = new ServiceCandidate("sc1", behavior);

        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component)
                .assignServiceCandidate(candidate, component)
                .build();
        
        // Perform the simulation
        var listener = new StateMonitoringListener();
        var worker = new TraceSimulatorWorker(listener, inputEvents, deploymentModel); 
        
        if (expectedOutcome == ExpectedOutcome.ERROR) {
            // If an error is expected, ensure that it occurs
            var exception = assertThrows(TraceProcessingException.class, worker::processEvents);
            assertTrue(exception.getMessage().contains(expectedErrorMessageFragment));            
        } else {
            // Otherwise, process the events and inspect the result
            worker.processEvents();

            // Create the transactions to match against
            var surroundingTransaction = new TopLevelTransaction("tx1", transactionStartEvent, location, Demarcation.EXPLICIT);

            var expectedTransaction = switch (expectedOutcome) {
            case NO_TRANSACTION -> null;
            case SAME_TRANSACTION -> surroundingTransaction;
            case NEW_TRANSACTION -> new TopLevelTransaction("synthetic-0", candidateEntryEvent, location, Demarcation.IMPLICIT);
            case SUBORDINATE_TRANSACTION -> null;
            case ERROR -> null;
            };
            
            var expectedStates = List.<SimulationState> of(
                    new SimulationState(useCaseStartEvent, null, component, location, null),
                    new SimulationState(transactionStartEvent, null, component, location, surroundingTransaction),
                    new SimulationState(candidateInvocationEvent, null, component, location, surroundingTransaction),
                    new SimulationState(candidateEntryEvent, candidate, component, location, expectedTransaction),
                    new SimulationState(entityReadEvent, candidate, component, location, expectedTransaction),
                    new SimulationState(entityWriteEvent, candidate, component, location, expectedTransaction),
                    new SimulationState(candidateExitEvent, candidate, component, location, expectedTransaction),
                    new SimulationState(candidateReturnEvent, null, component, location, surroundingTransaction),
                    new SimulationState(transactionCommitEvent, null, component, location, surroundingTransaction),
                    new SimulationState(useCaseEndEvent, null, component, location, null)
                    );

            var assumedStates = listener.assumedStates();        
            assertEquals(expectedStates, assumedStates);
        }
    }
    
    static Stream<Arguments> noTransactionArguments() {
        return Stream.of(
                arguments(TransactionBehavior.MANDATORY, ExpectedOutcome.ERROR, "No active transaction found for"),
                arguments(TransactionBehavior.NEVER, ExpectedOutcome.NO_TRANSACTION, null),
                arguments(TransactionBehavior.NOT_SUPPORTED, ExpectedOutcome.NO_TRANSACTION, null),
                arguments(TransactionBehavior.REQUIRED, ExpectedOutcome.NEW_TRANSACTION, null),
                arguments(TransactionBehavior.REQUIRES_NEW, ExpectedOutcome.NEW_TRANSACTION, null),
                arguments(TransactionBehavior.SUPPORTED, ExpectedOutcome.NO_TRANSACTION, null)
                );
    }
        
    /**
     * Test cases: Different transaction behaviors (see {@link TransactionBehavior}) when no transaction is active. 
     */
    @ParameterizedTest
    @MethodSource("noTransactionArguments")
    void behaviorWithNoTransaction(TransactionBehavior behavior, ExpectedOutcome expectedOutcome, String expectedErrorMessageFragment) {
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
        var candidate = new ServiceCandidate("sc1", behavior);

        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component)
                .assignServiceCandidate(candidate, component)
                .build();
        
        // Perform the simulation
        var listener = new StateMonitoringListener();
        var worker = new TraceSimulatorWorker(listener, inputEvents, deploymentModel); 
        
        if (expectedOutcome == ExpectedOutcome.ERROR) {
            // If an error is expected, ensure that it occurs
            var exception = assertThrows(TraceProcessingException.class, worker::processEvents);
            assertTrue(exception.getMessage().contains(expectedErrorMessageFragment));            
        } else {
            // Otherwise, process the events and inspect the result
            worker.processEvents();

            // Create the transaction to match against
            var expectedTransaction = switch (expectedOutcome) {
            case NO_TRANSACTION -> null;
            case SAME_TRANSACTION -> null;
            case NEW_TRANSACTION -> new TopLevelTransaction("synthetic-0", candidateEntryEvent, location, Demarcation.IMPLICIT);
            case SUBORDINATE_TRANSACTION -> null;
            case ERROR -> null;
            };
            
            var expectedStates = List.<SimulationState> of(
                    new SimulationState(useCaseStartEvent, null, component, location, null),
                    new SimulationState(candidateInvocationEvent, null, component, location, null),
                    new SimulationState(candidateEntryEvent, candidate, component, location, expectedTransaction),
                    new SimulationState(entityReadEvent, candidate, component, location, expectedTransaction),
                    new SimulationState(entityWriteEvent, candidate, component, location, expectedTransaction),
                    new SimulationState(candidateExitEvent, candidate, component, location, expectedTransaction),
                    new SimulationState(candidateReturnEvent, null, component, location, null),
                    new SimulationState(useCaseEndEvent, null, component, location, null)
                    );

            var assumedStates = listener.assumedStates();        
            assertEquals(expectedStates, assumedStates);
        }
    }
     
    static Stream<Arguments> propagatedTransactionArguments() {
        return Stream.of(
                arguments(TransactionBehavior.MANDATORY, ExpectedOutcome.SUBORDINATE_TRANSACTION, null),
                arguments(TransactionBehavior.NEVER, ExpectedOutcome.ERROR, "Active transaction found for"),
                arguments(TransactionBehavior.NOT_SUPPORTED, ExpectedOutcome.NO_TRANSACTION, null),
                arguments(TransactionBehavior.REQUIRED, ExpectedOutcome.SUBORDINATE_TRANSACTION, null),
                arguments(TransactionBehavior.REQUIRES_NEW, ExpectedOutcome.NEW_TRANSACTION, null),
                arguments(TransactionBehavior.SUPPORTED, ExpectedOutcome.SUBORDINATE_TRANSACTION, null)
                );
    }
        
    /**
     * Test cases: Different transaction behaviors (see {@link TransactionBehavior}) when a propagated transaction is available. 
     */
    @ParameterizedTest
    @MethodSource("propagatedTransactionArguments")
    void behaviorWithPropagatedTransaction(TransactionBehavior behavior, ExpectedOutcome expectedOutcome, String expectedErrorMessageFragment) {
        // Define the individual events
        var traceId = 1234L;
        var location1 = new ProcessLocation("test", 1234, 1);
        var location2 = new ProcessLocation("test", 2345, 1);
        
        // Define the individual events
        var useCaseStartEvent = new UseCaseStartEvent(traceId, 0, location1, "uc1");
        var transactionStartEvent = new TransactionStartEvent(traceId, 1, location1, "tx1");
        var candidateInvocationEvent = new ServiceCandidateInvocationEvent(traceId, 2, location1, "sc1");
        var candidateEntryEvent = new ServiceCandidateEntryEvent(traceId, 3, location2, "sc1");
        var entityReadEvent = new EntityReadEvent(traceId, 4, location2, "et1", "1");
        var entityWriteEvent = new EntityReadEvent(traceId, 5, location2, "et1", "1");
        var candidateExitEvent = new ServiceCandidateExitEvent(traceId, 6, location2, "sc1");
        var candidateReturnEvent = new ServiceCandidateReturnEvent(traceId, 7, location1, "sc1");
        var transactionCommitEvent = new TransactionCommitEvent(traceId, 8, location1, "tx1");
        var useCaseEndEvent = new UseCaseEndEvent(traceId, 9, location1, "uc1");
        
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
        var component1 = new Component("c1");
        var component2 = new Component("c2");
        var useCase = new UseCase("uc1");        
        var candidate = new ServiceCandidate("sc1", behavior);

        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component1)
                .assignServiceCandidate(candidate, component2)
                .addSymmetricRemoteConnection(component1, component2, 0, TransactionPropagation.SUBORDINATE)
                .build();
        
        // Perform the simulation
        var listener = new StateMonitoringListener();
        var worker = new TraceSimulatorWorker(listener, inputEvents, deploymentModel); 
        
        if (expectedOutcome == ExpectedOutcome.ERROR) {
            // If an error is expected, ensure that it occurs
            var exception = assertThrows(TraceProcessingException.class, worker::processEvents);
            assertTrue(exception.getMessage().contains(expectedErrorMessageFragment));            
        } else {
            // Otherwise, process the events and inspect the result
            worker.processEvents();

            // Create the transactions to match against
            var surroundingTransaction = new TopLevelTransaction("tx1", transactionStartEvent, location1, Demarcation.EXPLICIT);

            var expectedTransaction = switch (expectedOutcome) {
            case NO_TRANSACTION -> null;
            case SAME_TRANSACTION -> surroundingTransaction;
            case NEW_TRANSACTION -> new TopLevelTransaction("synthetic-0", candidateEntryEvent, location2, Demarcation.IMPLICIT);
            case SUBORDINATE_TRANSACTION -> new SubordinateTransaction("synthetic-0", candidateEntryEvent, location2, surroundingTransaction);
            case ERROR -> null;
            };
            
            var expectedStates = List.<SimulationState> of(
                    new SimulationState(useCaseStartEvent, null, component1, location1, null),
                    new SimulationState(transactionStartEvent, null, component1, location1, surroundingTransaction),
                    new SimulationState(candidateInvocationEvent, null, component1, location1, surroundingTransaction),
                    new SimulationState(candidateEntryEvent, candidate, component2, location2, expectedTransaction),
                    new SimulationState(entityReadEvent, candidate, component2, location2, expectedTransaction),
                    new SimulationState(entityWriteEvent, candidate, component2, location2, expectedTransaction),
                    new SimulationState(candidateExitEvent, candidate, component2, location2, expectedTransaction),
                    new SimulationState(candidateReturnEvent, null, component1, location1, surroundingTransaction),
                    new SimulationState(transactionCommitEvent, null, component1, location1, surroundingTransaction),
                    new SimulationState(useCaseEndEvent, null, component1, location1, null)
                    );

            var assumedStates = listener.assumedStates();        
            assertEquals(expectedStates, assumedStates);
        }
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

        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component)
                .build();
        
        // Perform the simulation
        var worker = new TraceSimulatorWorker(Collections.emptyList(), inputEvents, deploymentModel); 
        var exception = assertThrows(TraceProcessingException.class, () -> worker.processEvents());
        
        // Make sure that the exception has the expected message and occurs at the expected event
        assertEquals(offendingEvent, exception.offendingEvent());
        assertTrue(exception.getMessage().contains("A transaction was active"));
    }
    
    /**
     * Test case: A successful commit of an explicit transaction is communicated to the listener.
     */
    @Test
    void successfulCommitOfExplicitTransaction() {
        final var traceId = 1234L;
        final var location = new ProcessLocation("test", 1234, 1);
        
        // Define the individual events
        var useCaseStartEvent = new UseCaseStartEvent(traceId, 0, location, "uc1");
        var transactionStartEvent = new TransactionStartEvent(traceId, 50, location, "tx1");
        var transactionCommitEvent = new TransactionCommitEvent(traceId, 100, location, "tx1");
        var useCaseEndEvent = new UseCaseEndEvent(traceId, 200, location, "uc1");

        // Build the input trace
        var inputEvents = List.<MonitoringEvent> of(
                useCaseStartEvent,
                transactionStartEvent,
                transactionCommitEvent,
                useCaseEndEvent
                );        
        
        // Build the corresponding deployment model
        var component = new Component("c1");        
        var useCase = new UseCase("uc1");        

        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component)
                .build();
        
        // Build a listener to inspect the commit
        var listener = new TransactionEventListener();
        
        // Perform the simulation
        var worker = new TraceSimulatorWorker(listener, inputEvents, deploymentModel); 
        worker.processEvents();
                
        // Ensure that the transaction was committed at the expected event
        var expectedTransaction = new TopLevelTransaction("tx1", transactionStartEvent, location, Demarcation.EXPLICIT);
        
        assertEquals(transactionCommitEvent, listener.event);
        assertEquals(expectedTransaction, listener.committedTransaction);
        assertNull(listener.abortedTransaction);
    }
    
    /**
     * Test case: A failed commit of an explicit transaction is communicated to the listener.
     */
    @Test
    void failedCommitOfExplicitTransaction() {
        final var traceId = 1234L;
        final var location = new ProcessLocation("test", 1234, 1);
        
        // Define the individual events
        var useCaseStartEvent = new UseCaseStartEvent(traceId, 0, location, "uc1");
        var transactionStartEvent = new TransactionStartEvent(traceId, 50, location, "tx1");
        // Implicit abort events are also allowed in explicit top-level transactions, since they may occur when "internalizing" an implicit transaction
        var transactionAbortHint = new ImplicitTransactionAbortEvent(traceId, 90, location, "tx1", "cause");
        var transactionCommitEvent = new TransactionCommitEvent(traceId, 100, location, "tx1");
        var useCaseEndEvent = new UseCaseEndEvent(traceId, 200, location, "uc1");

        // Build the input trace
        var inputEvents = List.<MonitoringEvent> of(
                useCaseStartEvent,
                transactionStartEvent,
                transactionAbortHint,
                transactionCommitEvent,
                useCaseEndEvent
                );        
        
        // Build the corresponding deployment model
        var component = new Component("c1");        
        var useCase = new UseCase("uc1");        

        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component)
                .build();
        
        // Build a listener to inspect the commit
        var listener = new TransactionEventListener();
        
        // Perform the simulation
        var worker = new TraceSimulatorWorker(listener, inputEvents, deploymentModel); 
        worker.processEvents();
                
        // Ensure that the transaction was committed at the expected event
        var expectedTransaction = new TopLevelTransaction("tx1", transactionStartEvent, location, Demarcation.EXPLICIT);
        
        assertEquals(transactionCommitEvent, listener.event);
        assertNull(listener.committedTransaction);
        assertEquals(expectedTransaction, listener.abortedTransaction);        
    }
    
    /**
     * Test case: An explicit abort of an explicit transaction is communicated to the listener.
     */
    @Test
    void abortOfExplicitTransaction() {
        final var traceId = 1234L;
        final var location = new ProcessLocation("test", 1234, 1);
        
        // Define the individual events
        var useCaseStartEvent = new UseCaseStartEvent(traceId, 0, location, "uc1");
        var transactionStartEvent = new TransactionStartEvent(traceId, 50, location, "tx1");
        var transactionAbortEvent = new ExplicitTransactionAbortEvent(traceId, 100, location, "tx1");
        var useCaseEndEvent = new UseCaseEndEvent(traceId, 200, location, "uc1");

        // Build the input trace
        var inputEvents = List.<MonitoringEvent> of(
                useCaseStartEvent,
                transactionStartEvent,
                transactionAbortEvent,
                useCaseEndEvent
                );        
        
        // Build the corresponding deployment model
        var component = new Component("c1");        
        var useCase = new UseCase("uc1");        

        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component)
                .build();
        
        // Build a listener to inspect the commit
        var listener = new TransactionEventListener();
        
        // Perform the simulation
        var worker = new TraceSimulatorWorker(listener, inputEvents, deploymentModel); 
        worker.processEvents();
                
        // Ensure that the transaction was committed at the expected event
        var expectedTransaction = new TopLevelTransaction("tx1", transactionStartEvent, location, Demarcation.EXPLICIT);
        
        assertEquals(transactionAbortEvent, listener.event);
        assertNull(listener.committedTransaction);
        assertEquals(expectedTransaction, listener.abortedTransaction);        
    }
    
    /**
     * Test case: A successful commit of an implicit transaction is communicated to the listener.
     */
    @Test
    void successfulCommitOfImplicitTransaction() {
        final var traceId = 1234L;
        final var location = new ProcessLocation("test", 1234, 1);
        
        // Define the individual events
        var useCaseStartEvent = new UseCaseStartEvent(traceId, 0, location, "uc1");
        var candidateInvocationEvent = new ServiceCandidateInvocationEvent(traceId, 50, location, "sc1");
        var candidateEntryEvent = new ServiceCandidateEntryEvent(traceId, 50, location, "sc1");
        var candidateExitEvent = new ServiceCandidateExitEvent(traceId, 50, location, "sc1");
        var candidateReturnEvent = new ServiceCandidateReturnEvent(traceId, 50, location, "sc1");
        var useCaseEndEvent = new UseCaseEndEvent(traceId, 200, location, "uc1");

        // Build the input trace
        var inputEvents = List.<MonitoringEvent> of(
                useCaseStartEvent,
                candidateInvocationEvent,
                candidateEntryEvent,
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
        
        // Build a listener to inspect the commit
        var listener = new TransactionEventListener();
        
        // Perform the simulation
        var worker = new TraceSimulatorWorker(listener, inputEvents, deploymentModel); 
        worker.processEvents();
                
        // Ensure that the transaction was committed at the expected event
        var expectedTransaction = new TopLevelTransaction("synthetic-0", candidateEntryEvent, location, Demarcation.IMPLICIT);
        
        assertEquals(candidateExitEvent, listener.event);
        assertEquals(expectedTransaction, listener.committedTransaction);
        assertNull(listener.abortedTransaction);
    }
    
    /**
     * Test case: A failed commit of an implicit transaction is communicated to the listener.
     */
    @Test
    void failedCommitOfImplicitTransaction() {
        final var traceId = 1234L;
        final var location = new ProcessLocation("test", 1234, 1);
        
        // Define the individual events
        var useCaseStartEvent = new UseCaseStartEvent(traceId, 0, location, "uc1");
        var candidateInvocationEvent = new ServiceCandidateInvocationEvent(traceId, 50, location, "sc1");
        var candidateEntryEvent = new ServiceCandidateEntryEvent(traceId, 50, location, "sc1");
        var abortEvent = new ImplicitTransactionAbortEvent(traceId, 60, location, "synthetic-0", "cause");
        var candidateExitEvent = new ServiceCandidateExitEvent(traceId, 50, location, "sc1");
        var candidateReturnEvent = new ServiceCandidateReturnEvent(traceId, 50, location, "sc1");
        var useCaseEndEvent = new UseCaseEndEvent(traceId, 200, location, "uc1");

        // Build the input trace
        var inputEvents = List.<MonitoringEvent> of(
                useCaseStartEvent,
                candidateInvocationEvent,
                candidateEntryEvent,
                abortEvent,
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
        
        // Build a listener to inspect the commit
        var listener = new TransactionEventListener();
        
        // Perform the simulation
        var worker = new TraceSimulatorWorker(listener, inputEvents, deploymentModel); 
        worker.processEvents();
                
        // Ensure that the transaction was committed at the expected event
        var expectedTransaction = new TopLevelTransaction("synthetic-0", candidateEntryEvent, location, Demarcation.IMPLICIT);
                
        assertEquals(candidateExitEvent, listener.event);
        assertNull(listener.committedTransaction);
        assertEquals(expectedTransaction, listener.abortedTransaction);        
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
        public void onImplicitTransactionAbortEvent(ImplicitTransactionAbortEvent event, TraceSimulationContext context) {
            this.recordState(event, context);
        }
        
        @Override
        public void onExplicitTransactionAbortEvent(ExplicitTransactionAbortEvent event, TraceSimulationContext context) {
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
    
    private enum ExpectedOutcome {
        NO_TRANSACTION,
        SAME_TRANSACTION,
        SUBORDINATE_TRANSACTION,
        NEW_TRANSACTION,
        ERROR
    }
    
    private static class TransactionEventListener implements TraceSimulationListener {
        
        public MonitoringEvent event = null;
        
        public Transaction committedTransaction = null;
        
        public Transaction abortedTransaction = null;
        
        @Override
        public void onTransactionCommit(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
            this.event = event;
            this.committedTransaction = transaction;
            this.abortedTransaction = null;
        }
        
        @Override
        public void onTransactionAbort(MonitoringEvent event, Transaction transaction, TraceSimulationContext context) {
            this.event = event;
            this.committedTransaction = null;
            this.abortedTransaction = transaction;
        }        
        
    }
    
}
