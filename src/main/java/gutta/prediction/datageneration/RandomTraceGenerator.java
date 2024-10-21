package gutta.prediction.datageneration;

import gutta.prediction.datageneration.TransitionGraph.Edge;
import gutta.prediction.datageneration.TransitionGraph.StackWalkListener;
import gutta.prediction.datageneration.TransitionGraph.Vertex;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.Location;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class RandomTraceGenerator {

    private static final double ONE_TENTH = 1.0 / 10.0;
    
    private static final double ONE_FIFTH = 1.0 / 5.0;
    
    private static final List<UseCaseSpecification> USE_CASE_SPECS = specs();
    
    private static List<UseCaseSpecification> specs() {
        var serviceCandidate1 = new ServiceCandidate("Service Candidate 1", TransactionBehavior.SUPPORTED);
        var serviceCandidate2 = new ServiceCandidate("Service Candidate 2", TransactionBehavior.SUPPORTED);
        var serviceCandidate3 = new ServiceCandidate("Service Candidate 3", TransactionBehavior.SUPPORTED);
        var serviceCandidate4 = new ServiceCandidate("Service Candidate 4", TransactionBehavior.SUPPORTED);
        var serviceCandidate5 = new ServiceCandidate("Service Candidate 5", TransactionBehavior.SUPPORTED);
        var serviceCandidate6 = new ServiceCandidate("Service Candidate 6", TransactionBehavior.SUPPORTED);
        var serviceCandidate7 = new ServiceCandidate("Service Candidate 7", TransactionBehavior.SUPPORTED);
        var serviceCandidate8 = new ServiceCandidate("Service Candidate 8", TransactionBehavior.SUPPORTED);
        var serviceCandidate9 = new ServiceCandidate("Service Candidate 9", TransactionBehavior.SUPPORTED);
        var serviceCandidate10 = new ServiceCandidate("Service Candidate 10", TransactionBehavior.SUPPORTED);
        
        // First use case
        var useCase1 = new UseCase("Use Case 1");
        
        var vertex1_1 = new Vertex<>(serviceCandidate1);
        var vertex1_2 = new Vertex<>(serviceCandidate2);
        var vertex1_3 = new Vertex<>(serviceCandidate3);
        var vertex1_4 = new Vertex<>(serviceCandidate4);
        var vertex1_5 = new Vertex<>(serviceCandidate5);
        var vertex1_6 = new Vertex<>(serviceCandidate6);
        var vertex1_7 = new Vertex<>(serviceCandidate7);
        var vertex1_8 = new Vertex<>(serviceCandidate8);
        var vertex1_9 = new Vertex<>(serviceCandidate9);
        var vertex1_10 = new Vertex<>(serviceCandidate10);

        var edges1 = List.of(
                new Edge<>(ONE_TENTH, vertex1_1),
                new Edge<>(ONE_TENTH, vertex1_2),
                new Edge<>(ONE_TENTH, vertex1_3),
                new Edge<>(ONE_TENTH, vertex1_4),
                new Edge<>(ONE_TENTH, vertex1_5),
                new Edge<>(ONE_TENTH, vertex1_6),
                new Edge<>(ONE_TENTH, vertex1_7),
                new Edge<>(ONE_TENTH, vertex1_8),
                new Edge<>(ONE_TENTH, vertex1_9),
                new Edge<>(ONE_TENTH, vertex1_10)
        );
        
        vertex1_1.edges(edges1);
        vertex1_2.edges(edges1);
        vertex1_3.edges(edges1);
        vertex1_4.edges(edges1);
        vertex1_5.edges(edges1);
        vertex1_6.edges(edges1);
        vertex1_7.edges(edges1);
        vertex1_8.edges(edges1);
        vertex1_9.edges(edges1);
        vertex1_10.edges(edges1);
        
        var transitionGraph1 = new TransitionGraph<>(
                vertex1_1,
                vertex1_2,
                vertex1_3,
                vertex1_4,
                vertex1_5,
                vertex1_6,
                vertex1_7,
                vertex1_8,
                vertex1_9,
                vertex1_10
                );       
        
        transitionGraph1.validate();
        
        // Second use case
        
        var useCase2 = new UseCase("Use Case 2");
        
        var vertex2_1 = new Vertex<>(serviceCandidate2);
        var vertex2_2 = new Vertex<>(serviceCandidate4);
        var vertex2_3 = new Vertex<>(serviceCandidate6);
        var vertex2_4 = new Vertex<>(serviceCandidate8);
        var vertex2_5 = new Vertex<>(serviceCandidate10);

        var edges2 = List.of(
                new Edge<>(ONE_FIFTH, vertex1_1),
                new Edge<>(ONE_FIFTH, vertex1_2),
                new Edge<>(ONE_FIFTH, vertex1_3),
                new Edge<>(ONE_FIFTH, vertex1_4),
                new Edge<>(ONE_FIFTH, vertex1_5)
        );
        
        vertex2_1.edges(edges2);
        vertex2_2.edges(edges2);
        vertex2_3.edges(edges2);
        vertex2_4.edges(edges2);
        vertex2_5.edges(edges2);
        
        var transitionGraph2 = new TransitionGraph<>(
                vertex2_1,
                vertex2_2,
                vertex2_3,
                vertex2_4,
                vertex2_5
                );       
        
        transitionGraph2.validate();
        
        // Third use case
        
        var useCase3 = new UseCase("Use Case 3");
        
        var vertex3_1 = new Vertex<>(serviceCandidate1);
        var vertex3_2 = new Vertex<>(serviceCandidate3);
        var vertex3_3 = new Vertex<>(serviceCandidate5);
        var vertex3_4 = new Vertex<>(serviceCandidate7);
        var vertex3_5 = new Vertex<>(serviceCandidate9);

        var edges3 = List.of(
                new Edge<>(ONE_FIFTH, vertex3_1),
                new Edge<>(ONE_FIFTH, vertex3_2),
                new Edge<>(ONE_FIFTH, vertex3_3),
                new Edge<>(ONE_FIFTH, vertex3_4),
                new Edge<>(ONE_FIFTH, vertex3_5)
        );
        
        vertex3_1.edges(edges3);
        vertex3_2.edges(edges3);
        vertex3_3.edges(edges3);
        vertex3_4.edges(edges3);
        vertex3_5.edges(edges3);
        
        var transitionGraph3 = new TransitionGraph<>(
                vertex1_1,
                vertex1_2,
                vertex1_3,
                vertex1_4,
                vertex1_5,
                vertex1_6,
                vertex1_7,
                vertex1_8,
                vertex1_9,
                vertex1_10
                );       
        
        transitionGraph3.validate();
        
        return List.of(
                new UseCaseSpecification(useCase1, List.of(serviceCandidate1, serviceCandidate2, serviceCandidate3, serviceCandidate4, serviceCandidate5, serviceCandidate6, serviceCandidate7, serviceCandidate8, serviceCandidate9, serviceCandidate10), transitionGraph1, vertex1_1),
                new UseCaseSpecification(useCase2, List.of(serviceCandidate2, serviceCandidate4, serviceCandidate6, serviceCandidate8, serviceCandidate10), transitionGraph2, vertex2_1),
                new UseCaseSpecification(useCase3, List.of(serviceCandidate1, serviceCandidate3, serviceCandidate5, serviceCandidate7, serviceCandidate9), transitionGraph3, vertex3_1)
                );
    }
    
    private final Random random = new Random();
    
    public static void main(String[] arguments) throws IOException {
        var fileName = arguments[0];
                
        var numberOfTraces = Integer.parseInt(arguments[1]);
        var maxNumberOfInvocations = Integer.parseInt(arguments[2]);
        var maxInvocationDepth = Integer.parseInt(arguments[3]);
        
        new RandomTraceGenerator().generateRandomTraces(fileName, numberOfTraces, maxNumberOfInvocations, maxInvocationDepth);
    }
    
    private void generateRandomTraces(String fileName, int numberOfTraces, int maxNumberOfInvocations, int maxInvocationDepth) throws IOException {        
        var traces = new ArrayList<EventTrace>(numberOfTraces);
        for (var traceCount = 0; traceCount < numberOfTraces; traceCount++) {
            var trace = this.generateTrace(traceCount, maxNumberOfInvocations, maxInvocationDepth);
            traces.add(trace);
        }
        
        try (var outputStream = new FileOutputStream(fileName)) {
            new EventTraceEncoder().encodeTraces(traces, outputStream);
        }
    }
        
    private EventTrace generateTrace(long traceId, int maxNumberOfInvocations, int maxInvocationDepth) {
        var useCaseIndex = this.random.nextInt(USE_CASE_SPECS.size());
        var useCaseSpec = USE_CASE_SPECS.get(useCaseIndex);
        
        var numberOfInvocations = this.random.nextInt(maxNumberOfInvocations + 1);
        
        var useCase = useCaseSpec.useCase();
        var transitionGraph = useCaseSpec.transitionGraph();
        
        var events = new ArrayList<MonitoringEvent>();
        
        var timestampGenerator = new TimestampGenerator(10, 0, 0);
        var location = new ObservedLocation("test", 123, 45);
        
        events.add(new UseCaseStartEvent(traceId, timestampGenerator.nextStep(), location, useCase.name()));
                
        var invocationGenerator = new InvocationGenerator(traceId, timestampGenerator, location, events::add);
        transitionGraph.stackWalk(useCaseSpec.startVertex(), numberOfInvocations, maxInvocationDepth, invocationGenerator);
        
        events.add(new UseCaseEndEvent(traceId, timestampGenerator.nextStep(), location, useCase.name()));
        
        return EventTrace.of(events);
    }
    
    private static class InvocationGenerator implements StackWalkListener<ServiceCandidate> {
        
        private final long traceId;
        
        private final TimestampGenerator timestampGenerator;
        
        private final Location location;
        
        private final Consumer<MonitoringEvent> eventConsumer;
        
        public InvocationGenerator(long traceId, TimestampGenerator timestampGenerator, Location location, Consumer<MonitoringEvent> eventConsumer) {
            this.traceId = traceId;
            this.timestampGenerator = timestampGenerator;
            this.location = location;
            this.eventConsumer = eventConsumer;
        }
        
        @Override
        public void onVertexEntry(Vertex<ServiceCandidate> vertex) {
            var candidateName = vertex.label().name();
            
            var invocationEvent = new ServiceCandidateInvocationEvent(this.traceId, this.timestampGenerator.nextStep(), this.location, candidateName);
            var entryEvent = new ServiceCandidateEntryEvent(this.traceId, this.timestampGenerator.nextLatency(), this.location, candidateName);
            
            this.eventConsumer.accept(invocationEvent);
            this.eventConsumer.accept(entryEvent);
        }

        @Override
        public void onVertexExit(Vertex<ServiceCandidate> vertex) {
            var candidateName = vertex.label().name();
            
            var exitEvent = new ServiceCandidateExitEvent(this.traceId, this.timestampGenerator.nextStep(), this.location, candidateName);
            var returnEvent = new ServiceCandidateReturnEvent(this.traceId, this.timestampGenerator.nextLatency(), this.location, candidateName);
            
            this.eventConsumer.accept(exitEvent);
            this.eventConsumer.accept(returnEvent);            
        }
        
    }
    
    private record UseCaseSpecification(UseCase useCase, List<ServiceCandidate> serviceCandidates, TransitionGraph<ServiceCandidate> transitionGraph, Vertex<ServiceCandidate> startVertex) {}
    
}
