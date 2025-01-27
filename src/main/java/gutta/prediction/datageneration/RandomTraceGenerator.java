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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Trace generator to produce random traces based on a given deployment structure.
 */
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

        var location1 = new ObservedLocation("test1", 123, 1);
        var location2 = new ObservedLocation("test2", 456, 2);
        var location3 = new ObservedLocation("test3", 789, 3);

        var serviceAllocation = new HashMap<ServiceCandidate, Location>();
        serviceAllocation.put(serviceCandidate1, location1);
        serviceAllocation.put(serviceCandidate2, location2);
        serviceAllocation.put(serviceCandidate3, location3);
        serviceAllocation.put(serviceCandidate4, location1);
        serviceAllocation.put(serviceCandidate5, location2);
        serviceAllocation.put(serviceCandidate6, location3);
        serviceAllocation.put(serviceCandidate7, location1);
        serviceAllocation.put(serviceCandidate8, location2);
        serviceAllocation.put(serviceCandidate9, location3);
        serviceAllocation.put(serviceCandidate10, location1);

        // First use case
        var useCase1 = new UseCase("Use Case 1");

        var vertex11 = new Vertex<>(serviceCandidate1);
        var vertex12 = new Vertex<>(serviceCandidate2);
        var vertex13 = new Vertex<>(serviceCandidate3);
        var vertex14 = new Vertex<>(serviceCandidate4);
        var vertex15 = new Vertex<>(serviceCandidate5);
        var vertex16 = new Vertex<>(serviceCandidate6);
        var vertex17 = new Vertex<>(serviceCandidate7);
        var vertex18 = new Vertex<>(serviceCandidate8);
        var vertex19 = new Vertex<>(serviceCandidate9);
        var vertex110 = new Vertex<>(serviceCandidate10);

        var edges1 = List.of(//
                new Edge<>(ONE_TENTH, vertex11), new Edge<>(ONE_TENTH, vertex12), new Edge<>(ONE_TENTH, vertex13), new Edge<>(ONE_TENTH, vertex14),
                new Edge<>(ONE_TENTH, vertex15), new Edge<>(ONE_TENTH, vertex16), new Edge<>(ONE_TENTH, vertex17), new Edge<>(ONE_TENTH, vertex18),
                new Edge<>(ONE_TENTH, vertex19), new Edge<>(ONE_TENTH, vertex110));

        vertex11.edges(edges1);
        vertex12.edges(edges1);
        vertex13.edges(edges1);
        vertex14.edges(edges1);
        vertex15.edges(edges1);
        vertex16.edges(edges1);
        vertex17.edges(edges1);
        vertex18.edges(edges1);
        vertex19.edges(edges1);
        vertex110.edges(edges1);

        var transitionGraph1 = new TransitionGraph<>(//
                vertex11, vertex12, vertex13, vertex14, vertex15, vertex16, vertex17, vertex18, vertex19, vertex110);

        transitionGraph1.validate();

        var candidates1 = List.of(serviceCandidate1, serviceCandidate2, serviceCandidate3, serviceCandidate4, serviceCandidate5, serviceCandidate6,
                serviceCandidate7, serviceCandidate8, serviceCandidate9, serviceCandidate10);

        // Second use case

        var useCase2 = new UseCase("Use Case 2");

        var vertex21 = new Vertex<>(serviceCandidate2);
        var vertex22 = new Vertex<>(serviceCandidate4);
        var vertex23 = new Vertex<>(serviceCandidate6);
        var vertex24 = new Vertex<>(serviceCandidate8);
        var vertex25 = new Vertex<>(serviceCandidate10);

        var edges2 = List.of(//
                new Edge<>(ONE_FIFTH, vertex21), new Edge<>(ONE_FIFTH, vertex22), new Edge<>(ONE_FIFTH, vertex23), new Edge<>(ONE_FIFTH, vertex24),
                new Edge<>(ONE_FIFTH, vertex25));

        vertex21.edges(edges2);
        vertex22.edges(edges2);
        vertex23.edges(edges2);
        vertex24.edges(edges2);
        vertex25.edges(edges2);

        var transitionGraph2 = new TransitionGraph<>(vertex21, vertex22, vertex23, vertex24, vertex25);

        transitionGraph2.validate();

        var candidates2 = List.of(serviceCandidate2, serviceCandidate4, serviceCandidate6, serviceCandidate8, serviceCandidate10);

        // Third use case

        var useCase3 = new UseCase("Use Case 3");

        var vertex31 = new Vertex<>(serviceCandidate1);
        var vertex32 = new Vertex<>(serviceCandidate3);
        var vertex33 = new Vertex<>(serviceCandidate5);
        var vertex34 = new Vertex<>(serviceCandidate7);
        var vertex35 = new Vertex<>(serviceCandidate9);

        var edges3 = List.of(new Edge<>(ONE_FIFTH, vertex31), new Edge<>(ONE_FIFTH, vertex32), new Edge<>(ONE_FIFTH, vertex33), new Edge<>(ONE_FIFTH, vertex34),
                new Edge<>(ONE_FIFTH, vertex35));

        vertex31.edges(edges3);
        vertex32.edges(edges3);
        vertex33.edges(edges3);
        vertex34.edges(edges3);
        vertex35.edges(edges3);

        var transitionGraph3 = new TransitionGraph<>(vertex31, vertex32, vertex33, vertex34, vertex35);

        transitionGraph3.validate();

        var candidates3 = List.of(serviceCandidate1, serviceCandidate3, serviceCandidate5, serviceCandidate7, serviceCandidate9);

        return List.of(//
                new UseCaseSpecification(useCase1, candidates1, location1, serviceAllocation, transitionGraph1, vertex11),
                new UseCaseSpecification(useCase2, candidates2, location2, serviceAllocation, transitionGraph2, vertex21, 10, 20),
                new UseCaseSpecification(useCase3, candidates3, location3, serviceAllocation, transitionGraph3, vertex31));
    }

    private final Random random = new Random();

    /**
     * Runs the trace generator.
     * 
     * @param arguments The command line arguments of the generator
     * @throws IOException If an I/O error occurs during trace generation
     */
    public static void main(String[] arguments) throws IOException {
        var fileName = arguments[0];
        var deploymentModelFileName = arguments[1];

        var numberOfTraces = Integer.parseInt(arguments[2]);
        var maxNumberOfInvocations = Integer.parseInt(arguments[3]);
        var maxInvocationDepth = Integer.parseInt(arguments[4]);

        var generator = new RandomTraceGenerator();
        generator.writeDeploymentModel(deploymentModelFileName);
        generator.generateRandomTraces(fileName, numberOfTraces, maxNumberOfInvocations, maxInvocationDepth);
    }

    private void writeDeploymentModel(String fileName) throws IOException {
        var modelSpec = "component \"Component 1\" {\n" + //
                "    useCase \"Use Case 1\"\n" + //
                "\n" + //
                "    serviceCandidate \"Service Candidate 1\"\n" + //
                "    serviceCandidate \"Service Candidate 4\"\n" + //
                "    serviceCandidate \"Service Candidate 7\"\n" + //
                "    serviceCandidate \"Service Candidate 10\"\n" + //
                "}\n" + //
                "\n" + //
                "component \"Component 2\" {\n" + //
                "    useCase \"Use Case 2\"\n" + //
                "\n" + //
                "    serviceCandidate \"Service Candidate 2\"\n" + //
                "    serviceCandidate \"Service Candidate 5\"\n" + //
                "    serviceCandidate \"Service Candidate 8\"\n" + //
                "}\n" + //
                "\n" + //
                "component \"Component 3\" {\n" + //
                "    useCase \"Use Case 3\"\n" + //
                "\n" + //
                "    serviceCandidate \"Service Candidate 3\"\n" + //
                "    serviceCandidate \"Service Candidate 6\"\n" + //
                "    serviceCandidate \"Service Candidate 9\"\n" + //
                "}\n" + //
                "\n" + //
                "remote \"Component 1\" -> \"Component 2\" [\n" + //
                "    overhead = 0\n" + //
                "]\n" + //
                "\n" + //
                "remote \"Component 1\" -> \"Component 3\" [\n" + //
                "    overhead = 0\n" + //
                "]\n" + //
                "\n" + //
                "remote \"Component 2\" -> \"Component 3\" [\n" + //
                "    overhead = 0\n" + //
                "]"; //

        try (var writer = new FileWriter(fileName)) {
            writer.write(modelSpec);
        }
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
        var startLocation = useCaseSpec.useCaseLocation();

        var events = new ArrayList<MonitoringEvent>();

        var timestampGenerator = new TimestampGenerator(10, useCaseSpec.minOverhead(), useCaseSpec.maxOverhead());

        events.add(new UseCaseStartEvent(traceId, timestampGenerator.nextStep(), startLocation, useCase.name()));

        var invocationGenerator = new InvocationGenerator(traceId, timestampGenerator, startLocation, useCaseSpec.candidateAllocation(), events::add);
        transitionGraph.stackWalk(useCaseSpec.startVertex(), numberOfInvocations, maxInvocationDepth, invocationGenerator);

        events.add(new UseCaseEndEvent(traceId, timestampGenerator.nextStep(), startLocation, useCase.name()));

        return EventTrace.of(events);
    }

    private static class InvocationGenerator implements StackWalkListener<ServiceCandidate> {

        private final long traceId;

        private final TimestampGenerator timestampGenerator;

        private final Map<ServiceCandidate, Location> candidateAllocation;

        private final Consumer<MonitoringEvent> eventConsumer;

        private final Deque<Location> stack = new ArrayDeque<>();

        public InvocationGenerator(long traceId, TimestampGenerator timestampGenerator, Location startLocation,
                Map<ServiceCandidate, Location> candidateAllocation, Consumer<MonitoringEvent> eventConsumer) {
            this.traceId = traceId;
            this.timestampGenerator = timestampGenerator;
            this.candidateAllocation = candidateAllocation;
            this.eventConsumer = eventConsumer;
            this.stack.push(startLocation);
        }

        @Override
        public void onVertexEntry(Vertex<ServiceCandidate> vertex) {
            var candidateName = vertex.label().name();
            var sourceLocation = this.stack.peek();
            var targetLocation = this.candidateAllocation.get(vertex.label());

            var remoteInvocation = (!sourceLocation.equals(targetLocation));

            var invocationEvent = new ServiceCandidateInvocationEvent(this.traceId, this.timestampGenerator.nextStep(), sourceLocation, candidateName);
            var entryEvent = new ServiceCandidateEntryEvent(this.traceId,
                    ((remoteInvocation) ? this.timestampGenerator.nextOverhead() : this.timestampGenerator.noStep()), targetLocation, candidateName);

            this.eventConsumer.accept(invocationEvent);
            this.eventConsumer.accept(entryEvent);

            this.stack.push(targetLocation);
        }

        @Override
        public void onVertexExit(Vertex<ServiceCandidate> vertex) {
            var candidateName = vertex.label().name();
            var sourceLocation = this.stack.pop();
            var targetLocation = this.stack.peek();

            var remoteInvocation = (!sourceLocation.equals(targetLocation));

            var exitEvent = new ServiceCandidateExitEvent(this.traceId, this.timestampGenerator.nextStep(), sourceLocation, candidateName);
            var returnEvent = new ServiceCandidateReturnEvent(this.traceId,
                    ((remoteInvocation) ? this.timestampGenerator.nextOverhead() : this.timestampGenerator.noStep()), targetLocation, candidateName);

            this.eventConsumer.accept(exitEvent);
            this.eventConsumer.accept(returnEvent);
        }

    }

    private record UseCaseSpecification(UseCase useCase, List<ServiceCandidate> serviceCandidates, Location useCaseLocation,
            Map<ServiceCandidate, Location> candidateAllocation, TransitionGraph<ServiceCandidate> transitionGraph, Vertex<ServiceCandidate> startVertex,
            long minOverhead, long maxOverhead) {

        public UseCaseSpecification(UseCase useCase, List<ServiceCandidate> serviceCandidates, Location useCaseLocation,
                Map<ServiceCandidate, Location> candidateAllocation, TransitionGraph<ServiceCandidate> transitionGraph, Vertex<ServiceCandidate> startVertex) {
            this(useCase, serviceCandidates, useCaseLocation, candidateAllocation, transitionGraph, startVertex, 0, 0);
        }

    }

}
