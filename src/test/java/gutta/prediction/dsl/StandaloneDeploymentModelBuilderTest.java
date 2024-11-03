package gutta.prediction.dsl;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DataStore;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.EntityType;
import gutta.prediction.domain.ReadWriteConflictBehavior;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.TransactionPropagation;
import gutta.prediction.domain.UseCase;
import gutta.prediction.dsl.DeploymentModelBuilder.DeploymentModelParseException;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test cases for the class {@link StandaloneDeploymentModelBuilder}.
 */
class StandaloneDeploymentModelBuilderTest {
    
    @Test
    void simpleModel() {
        var input = "component Component1 {\n" +
                "    useCase UseCase\n" +
                "    serviceCandidate Candidate [\n" +
                "        transactionBehavior = REQUIRED\n" +
                "    ]\n" +
                "    async serviceCandidate Candidate2" +
                "}\n" +
                "component Component2 {\n" +
                "    entityType EntityType\n" +
                "    entityType SubType partOf EntityType\n" +                
                "}\n" +
                "dataStore DataStore {\n" +
                "    entityType EntityType\n" +
                "    entityType SubType\n" +
                "}\n" +
                "// A comment\n" +
                "remote Component1 -> Component2 [\n" +
                "    latency = 10\n" +
                "]";
        
        var parsedModel = this.parse(input);
        
        var useCase = new UseCase("UseCase");
        var serviceCandidate1 = new ServiceCandidate("Candidate", TransactionBehavior.REQUIRED);
        var serviceCandidate2 = new ServiceCandidate("Candidate2", TransactionBehavior.SUPPORTED, true);
        var dataStore = new DataStore("DataStore", ReadWriteConflictBehavior.STALE_READ);
        var entityType = new EntityType("EntityType");
        var entitySubType = new EntityType("SubType", entityType);
        var component1 = new Component("Component1");
        var component2 = new Component("Component2");
        
        var expectedModel = DeploymentModel.builder()
                .assignUseCaseToComponent(useCase, component1)
                .assignServiceCandidateToComponent(serviceCandidate1, component1)
                .assignServiceCandidateToComponent(serviceCandidate2, component1)
                .assignEntityTypeToComponent(entityType, component2)
                .assignEntityTypeToComponent(entitySubType, component2)
                .assignEntityTypeToDataStore(entityType, dataStore)
                .assignEntityTypeToDataStore(entitySubType, dataStore)
                .addSymmetricRemoteConnection(component1, component2, 10, TransactionPropagation.NONE)
                .build();
        
        assertEquals(expectedModel, parsedModel);
    }
    
    /**
     * Test case: Two components with the same name lead to an error.
     */
    @Test
    void duplicateComponent() {
        var input = "component Test {}\n"
                + "component \"Test\" {}";
        
        var exception = assertThrows(DeploymentModelParseException.class, () -> this.parse(input));
        assertEquals("2,0: Duplicate component 'Test'.", exception.getMessage());
    }
    
    /**
     * Test case: Two use cases with the same name lead to an error.
     */
    @Test
    void duplicateUseCase() {
        var input = "component Test1 {\n" +
                "    useCase Test\n" +
                "}\n" +
                "component Test2 {\n" +
                "    useCase \"Test\"\n" +
                "}\n";
        
        var exception = assertThrows(DeploymentModelParseException.class, () -> this.parse(input));
        assertEquals("5,4: Duplicate use case 'Test'.", exception.getMessage());
    }
    
    /**
     * Test case: Two properties with the same name lead to an error.
     */
    @Test
    void duplicateProperty() {
        var input = "component Component1 {}\n" +
                "component \"Component2\" {}\n" +
                "remote Component1 -> Component2 [\n" +
                "    property = 123\n" +
                "    \"property\" = 456\n" +
                "]";
        
        var exception = assertThrows(DeploymentModelParseException.class, () -> this.parse(input));
        assertEquals("5,4: Duplicate property 'property'.", exception.getMessage());
    }
    
    /**
     * Test case: A specification of an unsupported / unknown transaction behavior results in an error.
     */
    @Test
    void unsupportedTransactionBehavior() {
        var input = "component Component {\n" +
                "    useCase UseCase\n" +
                "    serviceCandidate Candidate [\n" +
                "        transactionBehavior = DOESNOTEXIST\n" +
                "    ]\n" +
                "}";
        
        var exception = assertThrows(DeploymentModelParseException.class, () -> this.parse(input));
        assertEquals("4,8: Unsupported transaction behavior 'DOESNOTEXIST'.", exception.getMessage());
    }
    
    /**
     * Test case: The source component of a component connection is missing => error.
     */
    @Test
    void missingSourceComponent() {
        var input = "component Component1 {}\n" +
                "component Component2 {}\n" +
                "local ComponentX -> Component2";
        
        var exception = assertThrows(DeploymentModelParseException.class, () -> this.parse(input));
        assertEquals("3,6: Component 'ComponentX' does not exist.", exception.getMessage());
    }
    
    /**
     * Test case: The target component of a component connection is missing => error.
     */
    @Test
    void missingTargetComponent() {
        var input = "component Component1 {}\n" +
                "component Component2 {}\n" +
                "local Component1 -> ComponentX";
        
        var exception = assertThrows(DeploymentModelParseException.class, () -> this.parse(input));
        assertEquals("3,20: Component 'ComponentX' does not exist.", exception.getMessage());
    }

        
    private DeploymentModel parse(String input) {
        var charStream = CharStreams.fromString(input);
        var lexer = new DeploymentModelLexer(charStream);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new DeploymentModelParser(tokenStream);
        
        parser.setErrorHandler(new BailErrorStrategy());
        
        var modelContext = parser.deploymentModel();
        
        var modelBuilder = new StandaloneDeploymentModelBuilder();
        modelContext.accept(modelBuilder);
        
        return modelBuilder.getBuiltModel();
    }

}
