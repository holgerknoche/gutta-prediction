package gutta.prediction.datageneration;

import gutta.prediction.event.EventTrace;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ObservedLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import gutta.prediction.event.codec.EventTraceEncoder;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class BatchTraceGenerator {

    private static final int COMMIT_INTERVAL = 1000;
    
    public static void main(String[] arguments) throws IOException {
        var traceFileName = arguments[0];
        var deploymentModelFileName = arguments[1];
        
        var generator = new BatchTraceGenerator(); 
        generator.generateTraces(traceFileName);
        generator.writeDeploymentModel(deploymentModelFileName);
    }
    
    private void writeDeploymentModel(String fileName) throws IOException {
        var modelSpec = 
                "component \"Component 1\" {\n" +
                "    useCase \"Batch Traces\"\n" +
                "}\n" +
                "component \"Component 2\" {\n" +
                "    serviceCandidate Sc1\n" +
                "}\n" +
                "remote \"Component 1\" -> \"Component 2\" [\n" +
                "    overhead = 20\n" +
                "    transactionPropagation = SUBORDINATE\n" +
                "]\n";
        
        try (var writer = new FileWriter(fileName)) {
            writer.write(modelSpec);
        }
    }
    
    private void generateTraces(String fileName) throws IOException {
        var traces = new ArrayList<EventTrace>();
        
        var useCaseName = "Batch Traces";
        
        traces.add(this.generateBatchTrace(useCaseName, 1234, 2500000));
        
        try (var outputStream = new FileOutputStream(fileName)) {
            new EventTraceEncoder().encodeTraces(traces, outputStream);
        }
    }
    
    private EventTrace generateBatchTrace(String useCaseName, long traceId, int numberOfInvocations) {
        var timestamps = new TimestampGenerator(20, 0, 0);
        
        var location1 = new ObservedLocation("test1", 123, 1);
        var location2 = new ObservedLocation("test2", 123, 1);

        var events = new ArrayList<MonitoringEvent>();
        
        events.add(new UseCaseStartEvent(traceId, timestamps.nextStep(), location1, useCaseName));
        //events.add(new TransactionStartEvent(traceId, timestamps.nextStep(), location1, "tx0"));
        
        for (var invocationIndex = 0; invocationIndex < numberOfInvocations; invocationIndex++) {
            var commitIntervalReached = ((invocationIndex % COMMIT_INTERVAL) == 0);
            if (commitIntervalReached && invocationIndex > 0) {
                //events.add(new TransactionStartEvent(traceId, timestamps.nextStep(), location1, "tx" + (invocationIndex / COMMIT_INTERVAL)));
            }
            
            events.add(new ServiceCandidateInvocationEvent(traceId, timestamps.nextStep(), location1, "Sc1"));
            events.add(new ServiceCandidateEntryEvent(traceId, timestamps.nextStep(), location2, "Sc1"));
            events.add(new ServiceCandidateExitEvent(traceId, timestamps.nextStep(), location2, "Sc1"));
            events.add(new ServiceCandidateReturnEvent(traceId, timestamps.nextStep(), location1, "Sc1"));
            
            if (commitIntervalReached && invocationIndex > 0) {
                //events.add(new TransactionCommitEvent(traceId, timestamps.nextStep(), location1, "tx" + (invocationIndex / COMMIT_INTERVAL)));
            }
        }
        
        if ((numberOfInvocations % COMMIT_INTERVAL) != 0) {
            //events.add(new TransactionCommitEvent(traceId, timestamps.nextStep(), location1, "tx" + (numberOfInvocations / COMMIT_INTERVAL)));
        }
        
        events.add(new UseCaseEndEvent(traceId, timestamps.nextStep(), location1, useCaseName));
        return EventTrace.of(events);
    }
    
}
