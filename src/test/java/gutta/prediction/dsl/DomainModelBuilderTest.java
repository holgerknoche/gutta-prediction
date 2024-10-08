package gutta.prediction.dsl;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.UseCase;
import gutta.prediction.dsl.DomainModelBuilder.DomainModelParseException;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test cases for the class {@link DomainModelBuilder}.
 */
class DomainModelBuilderTest {
    
    @Test
    void simpleModel() {
        var input = "Component component1 {\n" +
                "    UseCase usecase\n" +
                "    ServiceCandidate candidate1 {\n" +
                "        transactionBehavior = REQUIRED\n" +
                "    }\n" +
                "}";
        
        var parsedModel = this.parse(input);
        
        var useCase = new UseCase("usecase");
        var component = new Component("component1");
        
        var expectedModel = DeploymentModel.builder()
                .assignUseCase(useCase, component)
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
        
        var exception = assertThrows(DomainModelParseException.class, () -> this.parse(input));
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
        
        var exception = assertThrows(DomainModelParseException.class, () -> this.parse(input));
        assertEquals("5,4: Duplicate use case 'test'.", exception.getMessage());
    }
    
    private DeploymentModel parse(String input) {
        var charStream = CharStreams.fromString(input);
        var lexer = new DomainModelLexer(charStream);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new DomainModelParser(tokenStream);
        
        parser.setErrorHandler(new BailErrorStrategy());
        
        var modelContext = parser.domainModel();
        
        var modelBuilder = new DomainModelBuilder();
        modelContext.accept(modelBuilder);
        
        return modelBuilder.getBuiltModel();
    }

}
