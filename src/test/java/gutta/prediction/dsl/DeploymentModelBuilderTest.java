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
 * Test cases for the class {@link DeploymentModelBuilder}.
 */
class DeploymentModelBuilderTest {
    
    @Test
    void simpleModel() {
        var input = "Component component1 {\n" +
                "    UseCase usecase\n" +
                "    ServiceCandidate candidate [\n" +
                "        transactionBehavior = REQUIRED\n" +
                "    ]\n" +
                "}\n" +
                "Component component2 {}\n" +
                "DataStore store {\n" +
                "    EntityType type\n" +
                "}\n" +
                "remote component1 -> component2 [\n" +
                "    latency = 10\n" +
                "]";
        
        var parsedModel = this.parse(input);
        
        var useCase = new UseCase("usecase");
        var serviceCandidate = new ServiceCandidate("candidate", TransactionBehavior.REQUIRED);
        var dataStore = new DataStore("store", ReadWriteConflictBehavior.STALE_READ);
        var entityType = new EntityType("type");
        var component1 = new Component("component1");
        var component2 = new Component("component2");
        
        var expectedModel = DeploymentModel.builder()
                .assignUseCase(useCase, component1)
                .assignServiceCandidate(serviceCandidate, component1)
                .assignEntityType(entityType, dataStore)
                .addSymmetricRemoteConnection(component1, component2, 10, TransactionPropagation.NONE)
                .build();
        
        assertEquals(expectedModel, parsedModel);
    }
    
    /**
     * Test case: Two components with the same name lead to an error.
     */
    @Test
    void duplicateComponent() {
        var input = "Component test {}\n"
                + "Component \"test\" {}";
        
        var exception = assertThrows(DeploymentModelParseException.class, () -> this.parse(input));
        assertEquals("2,0: Duplicate component 'test'.", exception.getMessage());
    }
    
    /**
     * Test case: Two use cases with the same name lead to an error.
     */
    @Test
    void duplicateUseCase() {
        var input = "Component test1 {\n" +
                "    UseCase test\n" +
                "}\n" +
                "Component test2 {\n" +
                "    UseCase \"test\"\n" +
                "}\n";
        
        var exception = assertThrows(DeploymentModelParseException.class, () -> this.parse(input));
        assertEquals("5,4: Duplicate use case 'test'.", exception.getMessage());
    }
    
    /**
     * Test case: Two properties with the same name lead to an error.
     */
    @Test
    void duplicateProperty() {
        var input = "Component component1 {}\n" +
                "Component \"component2\" {}\n" +
                "remote component1 -> component2 [\n" +
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
        var input = "Component component {\n" +
                "    UseCase usecase\n" +
                "    ServiceCandidate candidate [\n" +
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
        var input = "Component component1 {}\n" +
                "Component component2 {}\n" +
                "local componentX -> component2";
        
        var exception = assertThrows(DeploymentModelParseException.class, () -> this.parse(input));
        assertEquals("3,6: Component 'componentX' does not exist.", exception.getMessage());
    }
    
    /**
     * Test case: The target component of a component connection is missing => error.
     */
    @Test
    void missingTargetComponent() {
        var input = "Component component1 {}\n" +
                "Component component2 {}\n" +
                "local component1 -> componentX";
        
        var exception = assertThrows(DeploymentModelParseException.class, () -> this.parse(input));
        assertEquals("3,20: Component 'componentX' does not exist.", exception.getMessage());
    }

        
    private DeploymentModel parse(String input) {
        var charStream = CharStreams.fromString(input);
        var lexer = new DeploymentModelLexer(charStream);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new DeploymentModelParser(tokenStream);
        
        parser.setErrorHandler(new BailErrorStrategy());
        
        var modelContext = parser.deploymentModel();
        
        var modelBuilder = new DeploymentModelBuilder();
        modelContext.accept(modelBuilder);
        
        return modelBuilder.getBuiltModel();
    }

}
