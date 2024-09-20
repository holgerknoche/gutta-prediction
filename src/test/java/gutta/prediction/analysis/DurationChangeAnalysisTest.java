package gutta.prediction.analysis;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.TransactionPropagation;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ProcessLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for the class {@link DurationChangeAnalysis}.
 */
class DurationChangeAnalysisTest {
        
    private static final String USE_CASE_NAME = "useCase";
    
    private static final UseCase USE_CASE = new UseCase(USE_CASE_NAME);
    
    private static final String SERVICE_CANDIDATE_NAME = "serviceCandidate";
    
    private static final ServiceCandidate SERVICE_CANIDATE = new ServiceCandidate(SERVICE_CANDIDATE_NAME, TransactionBehavior.SUPPORTED);
    
    private static final Component COMPONENT_1 = new Component("component1");
    
    private static final Component COMPONENT_2 = new Component("component2");
    
    private static final double SIGNIFICANCE_LEVEL = 0.05;
    
    /**
     * Test case: Analysis of the trivial case that no modifications are made.
     */
    @Test
    void analysisWithNoModifications() {
         var deploymentModel = buildTestDeploymentModel();
         
         List<EventTrace> traces = new ArrayList<>();
         for (var numberOfInvocations = 1; numberOfInvocations <= 20; numberOfInvocations++) {
             var trace = buildSequenceOfInvocations(numberOfInvocations, numberOfInvocations, 0L, 100L);
             traces.add(trace);
         }
         
         var analysisResult = new DurationChangeAnalysis().analyzeTraces(traces, deploymentModel, deploymentModel, SIGNIFICANCE_LEVEL);
         
         // Check the analysis results
         assertFalse(analysisResult.significantChange());
         assertEquals(1.0, analysisResult.pValue());
         assertEquals(1050.0, analysisResult.originalMean());
         assertEquals(1050.0, analysisResult.modifiedMean());
    }
    
    /**
     * Test case: Analysis where only an insignificant (with respect to the significance level) change in latency is applied.
     */
    @Test
    void analysisWithInsignificantModifications() {                  
         List<EventTrace> traces = new ArrayList<>();
         for (var numberOfInvocations = 1; numberOfInvocations <= 20; numberOfInvocations++) {
             var trace = buildSequenceOfInvocations(numberOfInvocations, numberOfInvocations, 0L, 100L);
             traces.add(trace);
         }
         
         var deploymentModel = buildTestDeploymentModel();
         var modifiedDeploymentModel = deploymentModel.applyModifications()
                 .addSymmetricRemoteConnection(COMPONENT_1, COMPONENT_2, 5, TransactionPropagation.NONE)
                 .build();                 
         
         var analysisResult = new DurationChangeAnalysis().analyzeTraces(traces, deploymentModel, modifiedDeploymentModel, SIGNIFICANCE_LEVEL);

         // Check the analysis results
         assertFalse(analysisResult.significantChange());
         assertEquals(1050.0, analysisResult.originalMean());
         assertEquals(1155.0, analysisResult.modifiedMean());
    }
    
    /**
     * Test case: Analysis where a significant (with respect to the significance level) change in latency is applied.
     */
    @Test
    void analysisWithSignificantModifications() {                  
         List<EventTrace> traces = new ArrayList<>();
         for (var numberOfInvocations = 1; numberOfInvocations <= 20; numberOfInvocations++) {
             var trace = buildSequenceOfInvocations(numberOfInvocations, numberOfInvocations, 0L, 100L);
             traces.add(trace);
         }
         
         var deploymentModel = buildTestDeploymentModel();
         var modifiedDeploymentModel = deploymentModel.applyModifications()
                 .addSymmetricRemoteConnection(COMPONENT_1, COMPONENT_2, 25, TransactionPropagation.NONE)
                 .build();                 
         
         var analysisResult = new DurationChangeAnalysis().analyzeTraces(traces, deploymentModel, modifiedDeploymentModel, SIGNIFICANCE_LEVEL);
        
         // Check the analysis results
         assertTrue(analysisResult.significantChange());
         assertEquals(1050.0, analysisResult.originalMean());
         assertEquals(1575.0, analysisResult.modifiedMean());
    }
    
    private static DeploymentModel buildTestDeploymentModel() {
        return new DeploymentModel.Builder()
                .assignUseCase(USE_CASE, COMPONENT_1)
                .assignServiceCandidate(SERVICE_CANIDATE, COMPONENT_2)
                .addLocalConnection(COMPONENT_1, COMPONENT_2)
                .build();
    }
    
    private static EventTrace buildSequenceOfInvocations(int numberOfInvocations, int traceId, long latencyPerInvocation, long durationPerInvocation) {
        var location = new ProcessLocation("test", 1234, 1);        
        var events = new ArrayList<MonitoringEvent>(numberOfInvocations * 4 + 2);                
        
        var currentTime = 0L;
        
        events.add(new UseCaseStartEvent(traceId, currentTime, location, USE_CASE_NAME));
        
        for (int invocationIndex = 0; invocationIndex < numberOfInvocations; invocationIndex++) {
            events.add(new ServiceCandidateInvocationEvent(traceId, currentTime, location, SERVICE_CANDIDATE_NAME));
            events.add(new ServiceCandidateEntryEvent(traceId, currentTime += latencyPerInvocation, location, SERVICE_CANDIDATE_NAME));
            events.add(new ServiceCandidateExitEvent(traceId, currentTime += durationPerInvocation, location, SERVICE_CANDIDATE_NAME));
            events.add(new ServiceCandidateReturnEvent(traceId, currentTime += latencyPerInvocation, location, SERVICE_CANDIDATE_NAME));
        }
        
        events.add(new UseCaseEndEvent(traceId, currentTime, location, USE_CASE_NAME));
        
        return EventTrace.of(events);
    }

}
