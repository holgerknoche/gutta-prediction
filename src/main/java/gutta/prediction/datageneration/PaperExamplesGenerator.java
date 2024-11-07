package gutta.prediction.datageneration;

import gutta.prediction.domain.Entity;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ImplicitTransactionAbortEvent;
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

public class PaperExamplesGenerator {
    
    private static final int NUMBER_OF_INVOCATIONS = 100;
    
    public static void main(String[] arguments) throws IOException {
        var traceFileName = arguments[0];
        var deploymentModelFileName = arguments[1];
        
        var generator = new PaperExamplesGenerator(); 
        generator.generateTraces(traceFileName);
        generator.writeDeploymentModel(deploymentModelFileName);
    }

    private void writeDeploymentModel(String fileName) throws IOException {
        var modelSpec = 
                "component \"Insurance Application\" {\n" +
                "  useCase \"Contract Import Job\"\n" +
                "  useCase \"Car Insurance Contract Creation\"\n" +
                "\n" +
                "  serviceCandidate findPerson\n" +
                "  serviceCandidate createCarContract [\n" +
                "    transactionBehavior = REQUIRED\n" +
                "  ]\n" +
                "  serviceCandidate createContract [\n" +
                "    transactionBehavior = REQUIRED\n" +
                "  ]\n" +
                "  serviceCandidate updateCustomerFlag\n" +
                "  serviceCandidate registerSale\n" +
                "\n" +
                "  entityType \"Car Contract\"\n" +
                "  entityType \"Contract\"\n" +
                "  entityType \"Holder Role\" partOf \"Contract\"\n" +
                "  entityType \"Car Owner Role\" partOf \"Contract\"\n" +
                "  entityType \"Person\"\n" +
                "  entityType \"Car Contract Component\" partOf \"Car Contract\"\n" +
                "}\n" +
                "\n" +
                "dataStore \"Database\" {\n" +
                "  entityType \"Car Contract\"\n" +
                "  entityType \"Contract\"\n" +
                "  entityType \"Holder Role\"\n" +
                "  entityType \"Car Owner Role\"\n" +
                "  entityType \"Person\"\n" +
                "  entityType \"Car Contract Component\"\n" +
                "}\n";
        
        try (var writer = new FileWriter(fileName)) {
            writer.write(modelSpec);
        }
    }
    
    private void generateTraces(String fileName) throws IOException {
        var traces = new ArrayList<EventTrace>();
        
        traces.add(this.buildBatchTrace(123));
        traces.add(this.buildBatchTrace(234));
        traces.add(this.buildBatchTrace(345));
        traces.add(this.buildContractCreationTrace(678));
        traces.add(this.buildFailingContractCreationTrace(789));
        
        try (var outputStream = new FileOutputStream(fileName)) {
            new EventTraceEncoder().encodeTraces(traces, outputStream);
        }
    }
    
    private EventTrace buildBatchTrace(long traceId) {
        var timestamps = new TimestampGenerator(20, 0, 0);
        var location = new ObservedLocation("test", 1234, 1);
        
        var useCaseName = "Contract Import Job";
        var candidateName = "findPerson";
        
        var events = new ArrayList<MonitoringEvent>();
        events.add(new UseCaseStartEvent(traceId, timestamps.nextStep(), location, useCaseName));
        
        for (var invocationCount = 0; invocationCount < NUMBER_OF_INVOCATIONS; invocationCount++) {
            events.add(new ServiceCandidateInvocationEvent(traceId, timestamps.nextStep(), location, candidateName));
            events.add(new ServiceCandidateEntryEvent(traceId, timestamps.noStep(), location, candidateName));
            events.add(new ServiceCandidateExitEvent(traceId, timestamps.nextStep(), location, candidateName));
            events.add(new ServiceCandidateReturnEvent(traceId, timestamps.noStep(), location, candidateName));
        }
        
        events.add(new UseCaseEndEvent(traceId, timestamps.nextStep(), location, useCaseName));
        
        return EventTrace.of(events);
    }
    
    private EventTrace buildContractCreationTrace(long traceId) {
        var location = new ObservedLocation("test", 1234, 1);
        
        var useCaseName = "Car Insurance Contract Creation";
        var carCreationServiceName = "createCarContract";
        var genericCreationServiceName = "createContract";
        var updateCustomerFlagServiceName = "updateCustomerFlag";
        var registerSaleServiceName = "registerSale";
        
        var carContractEntity = new Entity("Car Contract", "1234");
        var genericContractEntity = new Entity("Contract", "123");
        var holderRoleEntity = new Entity("Holder Role", "234", genericContractEntity);
        var ownerRoleEntity = new Entity("Car Owner Role", "345", genericContractEntity);
        var ownerEntity = new Entity("Person", "567");
        var componentEntity1 = new Entity("Car Contract Component", "2345", carContractEntity);
        var componentEntity2 = new Entity("Car Contract Component", "3456", carContractEntity);
        
        return EventTrace.of(
                new UseCaseStartEvent(traceId, 0, location, useCaseName),
                new ServiceCandidateInvocationEvent(traceId, 10, location, carCreationServiceName),
                new ServiceCandidateEntryEvent(traceId, 10, location, carCreationServiceName, true, "tx1"),
                new EntityWriteEvent(traceId, 25, location, carContractEntity),
                new ServiceCandidateInvocationEvent(traceId, 30, location, genericCreationServiceName),
                new ServiceCandidateEntryEvent(traceId, 30, location, genericCreationServiceName),
                new EntityWriteEvent(traceId, 40, location, genericContractEntity),
                new EntityWriteEvent(traceId, 45, location, holderRoleEntity),
                new EntityWriteEvent(traceId, 50, location, ownerRoleEntity),
                new EntityReadEvent(traceId, 80, location, ownerEntity),
                new ServiceCandidateInvocationEvent(traceId, 90, location, updateCustomerFlagServiceName),
                new ServiceCandidateEntryEvent(traceId, 90, location, updateCustomerFlagServiceName),
                new EntityWriteEvent(traceId, 100, location, ownerEntity),
                new ServiceCandidateExitEvent(traceId, 120, location, updateCustomerFlagServiceName),
                new ServiceCandidateReturnEvent(traceId, 120, location, updateCustomerFlagServiceName),
                new ServiceCandidateExitEvent(traceId, 140, location, genericCreationServiceName),
                new ServiceCandidateReturnEvent(traceId, 140, location, genericCreationServiceName),
                new EntityWriteEvent(traceId, 150, location, componentEntity1),
                new EntityWriteEvent(traceId, 170, location, componentEntity2),
                new ServiceCandidateInvocationEvent(traceId, 200, location, registerSaleServiceName),
                new ServiceCandidateEntryEvent(traceId, 200, location, registerSaleServiceName),
                new EntityReadEvent(traceId, 220, location, ownerEntity),
                new EntityReadEvent(traceId, 240, location, genericContractEntity),
                new ServiceCandidateExitEvent(traceId, 260, location, registerSaleServiceName),
                new ServiceCandidateReturnEvent(traceId, 260, location, registerSaleServiceName),
                new ServiceCandidateExitEvent(traceId, 280, location, carCreationServiceName),
                new ServiceCandidateReturnEvent(traceId, 280, location, carCreationServiceName),
                new UseCaseEndEvent(traceId, 300, location, useCaseName)
                );
    }
    
    private EventTrace buildFailingContractCreationTrace(long traceId) {
        var location = new ObservedLocation("test", 1234, 1);
        
        var useCaseName = "Car Insurance Contract Creation";
        var carCreationServiceName = "createCarContract";
        var genericCreationServiceName = "createContract";
        var updateCustomerFlagServiceName = "updateCustomerFlag";
        
        var carContractEntity = new Entity("Car Contract", "1234");
        var genericContractEntity = new Entity("Contract", "123");
        var holderRoleEntity = new Entity("Holder Role", "234", genericContractEntity);
        var ownerRoleEntity = new Entity("Car Owner Role", "345", genericContractEntity);
        var ownerEntity = new Entity("Person", "567");

        return EventTrace.of(
                new UseCaseStartEvent(traceId, 0, location, useCaseName),
                new ServiceCandidateInvocationEvent(traceId, 10, location, carCreationServiceName),
                new ServiceCandidateEntryEvent(traceId, 10, location, carCreationServiceName, true, "tx1"),
                new EntityWriteEvent(traceId, 25, location, carContractEntity),
                new ServiceCandidateInvocationEvent(traceId, 30, location, genericCreationServiceName),
                new ServiceCandidateEntryEvent(traceId, 30, location, genericCreationServiceName),
                new EntityWriteEvent(traceId, 40, location, genericContractEntity),
                new EntityWriteEvent(traceId, 45, location, holderRoleEntity),
                new EntityWriteEvent(traceId, 50, location, ownerRoleEntity),
                new EntityReadEvent(traceId, 80, location, ownerEntity),
                new ServiceCandidateInvocationEvent(traceId, 90, location, updateCustomerFlagServiceName),
                new ServiceCandidateEntryEvent(traceId, 90, location, updateCustomerFlagServiceName),
                new EntityWriteEvent(traceId, 100, location, ownerEntity),
                new ServiceCandidateExitEvent(traceId, 120, location, updateCustomerFlagServiceName),
                new ServiceCandidateReturnEvent(traceId, 120, location, updateCustomerFlagServiceName),
                new ServiceCandidateExitEvent(traceId, 140, location, genericCreationServiceName),
                new ServiceCandidateReturnEvent(traceId, 140, location, genericCreationServiceName),
                new ImplicitTransactionAbortEvent(traceId, 150, location, "", "Example Error"),
                new ServiceCandidateExitEvent(traceId, 180, location, carCreationServiceName),
                new ServiceCandidateReturnEvent(traceId, 180, location, carCreationServiceName),
                new UseCaseEndEvent(traceId, 200, location, useCaseName)
                );
    }

}
