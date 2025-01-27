package gutta.prediction.datageneration;

import gutta.prediction.domain.Entity;
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

/**
 * Trace generator to produce artificial traces with consistency issues.
 */
public class TracesWithConsistencyIssuesGenerator {

    /**
     * Runs the trace generator.
     * 
     * @param arguments The command line arguments of the generator
     * @throws IOException If an I/O error occurs during trace generation
     */
    public static void main(String[] arguments) throws IOException {
        var traceFileName = arguments[0];
        var deploymentModelFileName = arguments[1];

        var generator = new TracesWithConsistencyIssuesGenerator();
        generator.generateTraces(traceFileName);
        generator.writeDeploymentModel(deploymentModelFileName);
    }

    private void writeDeploymentModel(String fileName) throws IOException {
        var modelSpec = "component \"Component 1\" {\n" + //
                "    useCase \"Consistency Issues\"\n" + //
                "    serviceCandidate Sc2 [transactionBehavior=REQUIRES_NEW]\n" + //
                "\n" + //
                "    entityType RootType\n" + //
                "    entityType SubType1 partOf RootType\n" + //
                "    entityType SubType2 partOf RootType\n" + //
                "    entityType UnrelatedType\n" + //
                "}\n" + //
                "component \"Component 2\" {\n" + //
                "    serviceCandidate Sc1\n" + //
                "    entityType EntityType1\n" + //
                "}\n" + //
                "remote \"Component 1\" -> \"Component 2\" [\n" + //
                "    overhead = 20\n" + //
                "    transactionPropagation = SUBORDINATE\n" + //
                "]\n" + //
                "dataStore DataStore {\n" + //
                "    entityType EntityType1\n" + //
                "    entityType RootType\n" + //
                "    entityType SubType1\n" + //
                "    entityType SubType2\n" + //
                "    entityType UnrelatedType\n" + //
                "}"; //

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
        traces.add(this.buildTraceWithInterleavedAccessToAggregate(useCaseName, 1238));

        try (var outputStream = new FileOutputStream(fileName)) {
            new EventTraceEncoder().encodeTraces(traces, outputStream);
        }
    }

    private EventTrace buildTraceWithStaleRead(String useCaseName, long traceId) {
        var timestamps = new TimestampGenerator(20, 0, 0);

        var location1 = new ObservedLocation("test1", 123, 1);
        var location2 = new ObservedLocation("test2", 123, 1);

        var entity = new Entity("EntityType1", "1");

        return EventTrace.of(//
                new UseCaseStartEvent(traceId, timestamps.nextStep(), location1, useCaseName), //
                new TransactionStartEvent(traceId, timestamps.nextStep(), location1, "tx1"), //
                new EntityWriteEvent(traceId, timestamps.nextStep(), location1, entity), //
                new ServiceCandidateInvocationEvent(traceId, timestamps.nextStep(), location1, "Sc1"), //
                new ServiceCandidateEntryEvent(traceId, timestamps.nextStep(), location2, "Sc1"), //
                new EntityReadEvent(traceId, timestamps.nextStep(), location2, entity), //
                new ServiceCandidateExitEvent(traceId, timestamps.nextStep(), location2, "Sc1"), //
                new ServiceCandidateReturnEvent(traceId, timestamps.nextStep(), location1, "Sc1"), //
                new TransactionCommitEvent(traceId, timestamps.nextStep(), location1, "tx1"), //
                new UseCaseEndEvent(traceId, timestamps.nextStep(), location1, useCaseName) //
        );
    }

    private EventTrace buildTraceWithWritesInSubordinateTransaction(String useCaseName, long traceId) {
        var timestamps = new TimestampGenerator(20, 0, 0);

        var location1 = new ObservedLocation("test1", 123, 1);
        var location2 = new ObservedLocation("test2", 123, 1);

        var entity1 = new Entity("EntityType1", "1");
        var entity2 = new Entity("EntityType1", "2");

        return EventTrace.of(//
                new UseCaseStartEvent(traceId, timestamps.nextStep(), location1, useCaseName), //
                new TransactionStartEvent(traceId, timestamps.nextStep(), location1, "tx1"), //
                new EntityWriteEvent(traceId, timestamps.nextStep(), location1, entity1), //
                new ServiceCandidateInvocationEvent(traceId, timestamps.nextStep(), location1, "Sc1"), //
                new ServiceCandidateEntryEvent(traceId, timestamps.nextStep(), location2, "Sc1"), //
                new EntityWriteEvent(traceId, timestamps.nextStep(), location2, entity2), //
                new ServiceCandidateExitEvent(traceId, timestamps.nextStep(), location2, "Sc1"), //
                new ServiceCandidateReturnEvent(traceId, timestamps.nextStep(), location1, "Sc1"), //
                new ExplicitTransactionAbortEvent(traceId, timestamps.nextStep(), location1, "tx1"), //
                new UseCaseEndEvent(traceId, timestamps.nextStep(), location1, useCaseName) //
        );
    }

    private EventTrace buildTraceWithWriteConflict(String useCaseName, long traceId) {
        var timestamps = new TimestampGenerator(20, 0, 0);

        var location1 = new ObservedLocation("test1", 123, 1);
        var location2 = new ObservedLocation("test2", 123, 1);

        var entity1 = new Entity("EntityType1", "1");
        var entity2 = new Entity("EntityType1", "2");

        return EventTrace.of(//
                new UseCaseStartEvent(traceId, timestamps.nextStep(), location1, useCaseName), //
                new TransactionStartEvent(traceId, timestamps.nextStep(), location1, "tx1"), //
                new EntityWriteEvent(traceId, timestamps.nextStep(), location1, entity1), //
                new ServiceCandidateInvocationEvent(traceId, timestamps.nextStep(), location1, "Sc1"), //
                new ServiceCandidateEntryEvent(traceId, timestamps.nextStep(), location2, "Sc1"), //
                new EntityWriteEvent(traceId, timestamps.nextStep(), location2, entity1), //
                new EntityWriteEvent(traceId, timestamps.nextStep(), location2, entity2), //
                new ServiceCandidateExitEvent(traceId, timestamps.nextStep(), location2, "Sc1"), //
                new ServiceCandidateReturnEvent(traceId, timestamps.nextStep(), location1, "Sc1"), //
                new TransactionCommitEvent(traceId, timestamps.nextStep(), location1, "tx1"), //
                new UseCaseEndEvent(traceId, timestamps.nextStep(), location1, useCaseName) //
        );
    }

    private EventTrace buildTraceWithImplicitAbort(String useCaseName, long traceId) {
        var timestamps = new TimestampGenerator(20, 0, 0);

        var location1 = new ObservedLocation("test1", 123, 1);
        var location2 = new ObservedLocation("test2", 123, 1);

        var entity1 = new Entity("EntityType1", "1");
        var entity2 = new Entity("EntityType1", "2");

        return EventTrace.of(//
                new UseCaseStartEvent(traceId, timestamps.nextStep(), location1, useCaseName), //
                new TransactionStartEvent(traceId, timestamps.nextStep(), location1, "tx1"), //
                new EntityWriteEvent(traceId, timestamps.nextStep(), location1, entity1), //
                new ServiceCandidateInvocationEvent(traceId, timestamps.nextStep(), location1, "Sc2"), //
                new ServiceCandidateEntryEvent(traceId, timestamps.noStep(), location2, "Sc2", true, "tx2"), //
                new EntityWriteEvent(traceId, timestamps.nextStep(), location2, entity2), //
                new ImplicitTransactionAbortEvent(traceId, timestamps.nextStep(), location2, "tx2", "some error"), //
                new ServiceCandidateExitEvent(traceId, timestamps.nextStep(), location2, "Sc2"), //
                new ServiceCandidateReturnEvent(traceId, timestamps.noStep(), location1, "Sc2"), //
                new TransactionCommitEvent(traceId, timestamps.nextStep(), location1, "tx1"), //
                new UseCaseEndEvent(traceId, timestamps.nextStep(), location1, useCaseName) //
        );
    }

    private EventTrace buildTraceWithInterleavedAccessToAggregate(String useCaseName, long traceId) {
        var location = new ObservedLocation("test", 1, 0);

        var entity1 = new Entity("SubType1", "e1", true, "r1");
        var entity2 = new Entity("UnrelatedType", "e2");
        var entity3 = new Entity("SubType2", "e3", true, "r1");

        return EventTrace.of(//
                new UseCaseStartEvent(traceId, 0, location, useCaseName), //
                new TransactionStartEvent(traceId, 20, location, "tx1"), //
                new EntityWriteEvent(traceId, 40, location, entity1), //
                new ServiceCandidateInvocationEvent(traceId, 60, location, "Sc2"), //
                new ServiceCandidateEntryEvent(traceId, 60, location, "Sc2"), //
                new EntityWriteEvent(traceId, 80, location, entity2), //
                new ServiceCandidateExitEvent(traceId, 100, location, "Sc2"), //
                new ServiceCandidateReturnEvent(traceId, 100, location, "Sc2"), //
                new EntityWriteEvent(traceId, 120, location, entity3), //
                new TransactionCommitEvent(traceId, 140, location, "tx1"), //
                new UseCaseEndEvent(traceId, 160, location, useCaseName) //
        );
    }

}
