package gutta.prediction.analysis.overhead;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.TransactionPropagation;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ObservedLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for the class {@link OverheadAnalyzer}.
 */
class OverheadAnalyzerTest {
    
    /**
     * Test case: An empty trace results in zero duration and zero overhead.
     */
    @Test
    void emptyTrace() {
        var inputTrace = EventTrace.of();
        
        var deploymentModel = new DeploymentModel.Builder()
                .build();
        
        var result = new OverheadAnalyzer().analyzeTrace(inputTrace, deploymentModel);
        
        var expectedResult = new OverheadAnalyzer.Result(0, 0, 0, 0);
        assertEquals(expectedResult, result);
    }
    
    /**
     * Test case: A non-empty trace without any overhead results in non-zero duration and zero overhead.  
     */
    @Test
    void nonemptyTraceWithNoOverhead() {
        var traceId = 1234;
        var location = new ObservedLocation("test", 1234, 0);
        
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
                .assignUseCaseToComponent(useCase, component)
                .assignServiceCandidateToComponent(serviceCandidate, component)
                .build();
        
        var result = new OverheadAnalyzer().analyzeTrace(inputTrace, deploymentModel);
        
        var expectedResult = new OverheadAnalyzer.Result(500, 0, 0, 0);
        assertEquals(expectedResult, result);               
    }
    
    /**
     * Test case: A non-empty trace without any overhead results in non-zero duration and zero overhead.  
     */
    @Test
    void nonemptyTraceWithOverhead() {
        var traceId = 1234;
        var location = new ObservedLocation("test", 1234, 0);
        
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
                .assignUseCaseToComponent(useCase, component)
                .assignServiceCandidateToComponent(serviceCandidate, component)
                .build();
        
        var result = new OverheadAnalyzer().analyzeTrace(inputTrace, deploymentModel);
        
        var expectedResult = new OverheadAnalyzer.Result(1000, 250, 0.25f, 0);
        assertEquals(expectedResult, result);               
    }
    
    /**
     * Test case: Remote calls are identified and counted.
     */
    @Test
    void remoteCallCounting() {
        var traceId = 1234;
        var location1 = new ObservedLocation("test1", 1234, 0);
        var location2 = new ObservedLocation("test2", 1234, 1);
        
        var inputTrace = EventTrace.of(
                new UseCaseStartEvent(traceId, 0, location1, "uc1"),
                new ServiceCandidateInvocationEvent(traceId, 100, location1, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 200, location2, "sc1"),
                new ServiceCandidateExitEvent(traceId, 500, location2, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 650, location1, "sc1"),
                new ServiceCandidateInvocationEvent(traceId, 700, location1, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 800, location2, "sc1"),
                new ServiceCandidateExitEvent(traceId, 1000, location2, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 1100, location1, "sc1"),
                new UseCaseEndEvent(traceId, 1200, location1, "uc1")
                );
        
        var useCase = new UseCase("uc1");
        var serviceCandidate = new ServiceCandidate("sc1", TransactionBehavior.SUPPORTED);
        var component1 = new Component("c1");        
        var component2 = new Component("c2");
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCaseToComponent(useCase, component1)
                .assignServiceCandidateToComponent(serviceCandidate, component2)
                .addSymmetricRemoteConnection(component1, component2, 100, TransactionPropagation.NONE)
                .build();
        
        var result = new OverheadAnalyzer().analyzeTrace(inputTrace, deploymentModel);
        
        var expectedResult = new OverheadAnalyzer.Result(1200, 450, 0.375f, 2);
        assertEquals(expectedResult, result);
    }
    
    /**
     * Test case: An invocation of a asynchronous service candidate effectively has a duration of zero.
     */
    @Test
    void asynchronousInvocationHasZeroDuration() {
        var traceId = 1234;
        var location = new ObservedLocation("test", 1234, 0);
        
        var inputTrace = EventTrace.of(
                new UseCaseStartEvent(traceId, 0, location, "uc1"),
                new ServiceCandidateInvocationEvent(traceId, 100, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 200, location, "sc1"),
                new ServiceCandidateExitEvent(traceId, 500, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 600, location, "sc1"),
                new UseCaseEndEvent(traceId, 1000, location, "uc1")
                );
        
        var useCase = new UseCase("uc1");
        var serviceCandidate = new ServiceCandidate("sc1", TransactionBehavior.SUPPORTED, true);
        var component = new Component("c1");        
        
        var deploymentModel = new DeploymentModel.Builder()
                .assignUseCaseToComponent(useCase, component)
                .assignServiceCandidateToComponent(serviceCandidate, component)
                .build();
        
        var result = new OverheadAnalyzer().analyzeTrace(inputTrace, deploymentModel);
        
        var expectedResult = new OverheadAnalyzer.Result(500, 0, 0.0f, 0);
        assertEquals(expectedResult, result);
    }

}
