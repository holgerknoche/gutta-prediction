package gutta.prediction.analysis.latency;

import gutta.prediction.analysis.latency.LatencyAnalyzer;
import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ProcessLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for the class {@link LatencyAnalyzer}.
 */
class LatencyAnalyzerTest {
    
    /**
     * Test case: An empty trace results in zero duration and zero latency.
     */
    @Test
    void emptyTrace() {
        var inputTrace = EventTrace.of();
        
        var deploymentModel = new DeploymentModel.Builder()
                .build();
        
        var result = new LatencyAnalyzer().analyzeTrace(inputTrace, deploymentModel);
        
        var expectedResult = new LatencyAnalyzer.Result(0, 0, 0);
        assertEquals(expectedResult, result);
    }
    
    /**
     * Test case: A non-empty trace without any latency results in non-zero duration and zero latency.  
     */
    @Test
    void nonemptyTraceWithNoLatency() {
        var traceId = 1234;
        var location = new ProcessLocation("test", 1234, 0);
        
        var inputTrace = EventTrace.of(
                new UseCaseStartEvent(traceId, 0, location, "uc1"),
                new ServiceCandidateInvocationEvent(traceId, 100, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 100, location, "sc1"),
                new ServiceCandidateExitEvent(traceId, 400, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 400, location, "sc1"),
                new UseCaseEndEvent(traceId, 500, location, "uc1")
                );
        
        var useCase = new UseCase("uc1");
        var serviceCandidate = new ServiceCandidate("sc1", TransactionBehavior.SUPPORTED);
        var component = new Component("c1");        
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component)
                .assignServiceCandidate(serviceCandidate, component)
                .build();
        
        var result = new LatencyAnalyzer().analyzeTrace(inputTrace, deploymentModel);
        
        var expectedResult = new LatencyAnalyzer.Result(500, 0, 0);
        assertEquals(expectedResult, result);               
    }
    
    /**
     * Test case: A non-empty trace without any latency results in non-zero duration and zero latency.  
     */
    @Test
    void nonemptyTraceWithLatency() {
        var traceId = 1234;
        var location = new ProcessLocation("test", 1234, 0);
        
        var inputTrace = EventTrace.of(
                new UseCaseStartEvent(traceId, 0, location, "uc1"),
                new ServiceCandidateInvocationEvent(traceId, 100, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 200, location, "sc1"),
                new ServiceCandidateExitEvent(traceId, 500, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 650, location, "sc1"),
                new UseCaseEndEvent(traceId, 1000, location, "uc1")
                );
        
        var useCase = new UseCase("uc1");
        var serviceCandidate = new ServiceCandidate("sc1", TransactionBehavior.SUPPORTED);
        var component = new Component("c1");        
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase, component)
                .assignServiceCandidate(serviceCandidate, component)
                .build();
        
        var result = new LatencyAnalyzer().analyzeTrace(inputTrace, deploymentModel);
        
        var expectedResult = new LatencyAnalyzer.Result(1000, 250, 0.25f);
        assertEquals(expectedResult, result);               
    }

}
