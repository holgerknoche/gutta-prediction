package gutta.prediction.dsl;

import gutta.prediction.domain.DeploymentModel;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class DeploymentModelReader {
    
    public DeploymentModel readModel(String input) {
        return this.readModel(CharStreams.fromString(input));
    }
    
    private DeploymentModel readModel(CharStream inputStream) {
        var lexer = new DeploymentModelLexer(inputStream);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new DeploymentModelParser(tokenStream);
        
        var modelContext = parser.deploymentModel();
        
        var modelBuilder = new DeploymentModelBuilder();
        modelContext.accept(modelBuilder);
        
        return modelBuilder.getBuiltModel();
    }

}
