package gutta.prediction.dsl;

import gutta.prediction.dsl.DomainModelBuilder.DomainModelParseException;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test cases for the class {@link DomainModelBuilder}.
 */
class DomainModelBuilderTest {
    
    @Test
    void simpleModel() {
        var input = "Component test {}";
        
        this.parse(input);
    }
    
    /**
     * Test case: Two components with the same name lead to an error.
     */
    @Test
    void duplicateComponent() {
        var input = "Component test {}\n"
                + "Component 'test' {}";
        
        var exception = assertThrows(DomainModelParseException.class, () -> this.parse(input));
        assertEquals("2,0: Duplicate component 'test'.", exception.getMessage());
    }
    
    /**
     * Test case: Two use cases with the same name lead to an error.
     */
    @Test
    @Disabled
    void duplicateUseCase() {
        var input = "UseCase test\n"
                + "UseCase 'test'";
        
        var exception = assertThrows(DomainModelParseException.class, () -> this.parse(input));
        assertEquals("2,0: Duplicate use case 'test'.", exception.getMessage());
    }
    
    private void parse(String input) {
        var charStream = CharStreams.fromString(input);
        var lexer = new DomainModelLexer(charStream);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new DomainModelParser(tokenStream);
        
        parser.setErrorHandler(new BailErrorStrategy());
        
        var modelContext = parser.domainModel();
        
        var modelBuilder = new DomainModelBuilder();
        modelContext.accept(modelBuilder);
    }

}
