package gutta.prediction.stream;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ProcessLocation;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for the class {@link EventStreamProcessorWorker}.
 */
class EventStreamProcessorWorkerTest {
    
    /**
     * Test case: An error occurs when a transaction is already active when an explicitly demarcated transaction is started. 
     */
    @Test
    void activeTransactionWhenExplicitTransactionIsStarted() {        
        final var traceId = 1234L;
        final var location = new ProcessLocation("test", 1234, 1);

        // This event must have explicit transaction demarcation to trigger the error
        final var offendingEvent = new TransactionStartEvent(traceId, 100, location, "tx2"); 
        
        final var inputEvents = Arrays.<MonitoringEvent> asList(
                new UseCaseStartEvent(traceId, 0, location, "uc1"),
                new TransactionStartEvent(traceId, 50, location, "tx1"),
                offendingEvent,
                new UseCaseEndEvent(traceId, 200, location, "uc1")
                );
        
        var component = new Component("c1");
        
        var useCase = new UseCase("uc1");
        
        var candidate = new ServiceCandidate("sc1", TransactionBehavior.SUPPORTED);

        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component)
                .assignServiceCandidate(candidate, component)
                .build();
                       
        var worker = new EventStreamProcessorWorker(Collections.emptyList(), inputEvents, deploymentModel); 
        var exception = assertThrows(TraceProcessingException.class, () -> worker.processEvents());
        
        // Make sure that the exception has the expected message and occurs at the expected event
        assertEquals(offendingEvent, exception.offendingEvent());
        assertTrue(exception.getMessage().contains("A transaction was active"));
    }

}
