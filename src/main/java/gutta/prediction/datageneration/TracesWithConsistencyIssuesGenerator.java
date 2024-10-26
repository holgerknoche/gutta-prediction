package gutta.prediction.datageneration;

import gutta.prediction.domain.Entity;
import gutta.prediction.domain.EntityType;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ExplicitTransactionAbortEvent;
import gutta.prediction.event.ImplicitTransactionAbortEvent;
import gutta.prediction.event.ObservedLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import gutta.prediction.event.codec.EventTraceEncoder;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TracesWithConsistencyIssuesGenerator {
    
    public static void main(String[] arguments) throws IOException {
        var traceFileName = arguments[0];
        var deploymentModelFileName = arguments[1];
        
        var generator = new TracesWithConsistencyIssuesGenerator(); 
        generator.generateTraces(traceFileName);
        generator.writeDeploymentModel(deploymentModelFileName);
    }
    
    private void writeDeploymentModel(String fileName) throws IOException {
        var modelSpec = 
                "Component c1 {\n" +
                "    UseCase \"Consistency Issues\"\n" +
                "    ServiceCandidate sc2 [transactionBehavior=REQUIRES_NEW]" +
                "}\n" +
                "Component c2 {\n" +
                "    ServiceCandidate sc1\n" +
                "}\n" +
                "remote c1 -> c2 [\n" +
                "    latency = 20\n" +
                "    transactionPropagation = SUBORDINATE\n" +
                "]\n" +
                "DataStore ds {\n" +
                "    EntityType et1\n" +
                "}";
        
        try (var writer = new FileWriter(fileName)) {
            writer.write(modelSpec);
        }
    }
    
    private void generateTraces(String fileName) throws IOException {
        var traces = new ArrayList<EventTrace>();
        
        var useCaseName = "Consistency Issues";
                
        traces.add(this.buildTraceWithStaleRead(useCaseName, 1234));
        traces.add(this.buildTraceWithWritesInSubordinateTransaction(useCaseName, 1235));
        traces.add(this.buildTraceWithWriteConflict(useCaseName, 1236));
        traces.add(this.buildTraceWithImplicitAbort(useCaseName, 1237));
        
        try (var outputStream = new FileOutputStream(fileName)) {
            new EventTraceEncoder().encodeTraces(traces, outputStream);
        }
    }
    
    private EventTrace buildTraceWithStaleRead(String useCaseName, long traceId) {
        var timestamps = new TimestampGenerator(20, 0, 0);
        
        var location1 = new ObservedLocation("test1", 123, 1);
        var location2 = new ObservedLocation("test2", 123, 1);
        
        var entityType = new EntityType("et1");
        var entity = new Entity(entityType, "1");
        
        return EventTrace.of(
                new UseCaseStartEvent(traceId, timestamps.nextStep(), location1, useCaseName),
                new TransactionStartEvent(traceId, timestamps.nextStep(), location1, "tx1"),
                new EntityWriteEvent(traceId, timestamps.nextStep(), location1, entity),
                new ServiceCandidateInvocationEvent(traceId, timestamps.nextStep(), location1, "sc1"),
                new ServiceCandidateEntryEvent(traceId, timestamps.nextStep(), location2, "sc1"),
                new EntityReadEvent(traceId, timestamps.nextStep(), location2, entity),
                new ServiceCandidateExitEvent(traceId, timestamps.nextStep(), location2, "sc1"),
                new ServiceCandidateReturnEvent(traceId, timestamps.nextStep(), location1, "sc1"),
                new TransactionCommitEvent(traceId, timestamps.nextStep(), location1, "tx1"),
                new UseCaseEndEvent(traceId, timestamps.nextStep(), location1, useCaseName)
                );
    }
    
    private EventTrace buildTraceWithWritesInSubordinateTransaction(String useCaseName, long traceId) {
        var timestamps = new TimestampGenerator(20, 0, 0);
        
        var location1 = new ObservedLocation("test1", 123, 1);
        var location2 = new ObservedLocation("test2", 123, 1);
        
        var entityType = new EntityType("et1");
        var entity1 = new Entity(entityType, "1");
        var entity2 = new Entity(entityType, "2"); 
        
        return EventTrace.of(
                new UseCaseStartEvent(traceId, timestamps.nextStep(), location1, useCaseName),
                new TransactionStartEvent(traceId, timestamps.nextStep(), location1, "tx1"),
                new EntityWriteEvent(traceId, timestamps.nextStep(), location1, entity1),
                new ServiceCandidateInvocationEvent(traceId, timestamps.nextStep(), location1, "sc1"),
                new ServiceCandidateEntryEvent(traceId, timestamps.nextStep(), location2, "sc1"),
                new EntityWriteEvent(traceId, timestamps.nextStep(), location2, entity2),
                new ServiceCandidateExitEvent(traceId, timestamps.nextStep(), location2, "sc1"),
                new ServiceCandidateReturnEvent(traceId, timestamps.nextStep(), location1, "sc1"),
                new ExplicitTransactionAbortEvent(traceId, timestamps.nextStep(), location1, "tx1"),
                new UseCaseEndEvent(traceId, timestamps.nextStep(), location1, useCaseName)                
                );
    }
    
    private EventTrace buildTraceWithWriteConflict(String useCaseName, long traceId) {
        var timestamps = new TimestampGenerator(20, 0, 0);
        
        var location1 = new ObservedLocation("test1", 123, 1);
        var location2 = new ObservedLocation("test2", 123, 1);
        
        var entityType = new EntityType("et1");
        var entity1 = new Entity(entityType, "1");
        var entity2 = new Entity(entityType, "2");
        
        return EventTrace.of(
                new UseCaseStartEvent(traceId, timestamps.nextStep(), location1, useCaseName),
                new TransactionStartEvent(traceId, timestamps.nextStep(), location1, "tx1"),
                new EntityWriteEvent(traceId, timestamps.nextStep(), location1, entity1),
                new ServiceCandidateInvocationEvent(traceId, timestamps.nextStep(), location1, "sc1"),
                new ServiceCandidateEntryEvent(traceId, timestamps.nextStep(), location2, "sc1"),
                new EntityWriteEvent(traceId, timestamps.nextStep(), location2, entity1),
                new EntityWriteEvent(traceId, timestamps.nextStep(), location2, entity2),
                new ServiceCandidateExitEvent(traceId, timestamps.nextStep(), location2, "sc1"),
                new ServiceCandidateReturnEvent(traceId, timestamps.nextStep(), location1, "sc1"),
                new TransactionCommitEvent(traceId, timestamps.nextStep(), location1, "tx1"),
                new UseCaseEndEvent(traceId, timestamps.nextStep(), location1, useCaseName)                
                );
    }
    
    private EventTrace buildTraceWithImplicitAbort(String useCaseName, long traceId) {
        var timestamps = new TimestampGenerator(20, 0, 0);
        
        var location1 = new ObservedLocation("test1", 123, 1);
        var location2 = new ObservedLocation("test2", 123, 1);
        
        var entityType = new EntityType("et1");
        var entity1 = new Entity(entityType, "1");
        var entity2 = new Entity(entityType, "2");
        
        return EventTrace.of(
                new UseCaseStartEvent(traceId, timestamps.nextStep(), location1, useCaseName),
                new TransactionStartEvent(traceId, timestamps.nextStep(), location1, "tx1"),
                new EntityWriteEvent(traceId, timestamps.nextStep(), location1, entity1),
                new ServiceCandidateInvocationEvent(traceId, timestamps.nextStep(), location1, "sc2"),
                new ServiceCandidateEntryEvent(traceId, timestamps.noStep(), location2, "sc2", true, "tx2"),
                new EntityWriteEvent(traceId, timestamps.nextStep(), location2, entity2),
                new ImplicitTransactionAbortEvent(traceId, timestamps.nextStep(), location2, "tx2", "some error"),
                new ServiceCandidateExitEvent(traceId, timestamps.nextStep(), location2, "sc2"),
                new ServiceCandidateReturnEvent(traceId, timestamps.noStep(), location1, "sc2"),
                new TransactionCommitEvent(traceId, timestamps.nextStep(), location1, "tx1"),
                new UseCaseEndEvent(traceId, timestamps.nextStep(), location1, useCaseName)                
                );
    }

}
