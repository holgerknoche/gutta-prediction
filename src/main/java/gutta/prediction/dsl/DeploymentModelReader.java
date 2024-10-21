package gutta.prediction.dsl;

import gutta.prediction.domain.DeploymentModel;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class DeploymentModelReader {
    
    public DeploymentModel readModel(String input) {
        return this.readModel(CharStreams.fromString(input), null);
    }
    
    public DeploymentModel readModel(String input, DeploymentModel originalModel) {
        return this.readModel(CharStreams.fromString(input), originalModel);
    }
    
    private DeploymentModel readModel(CharStream inputStream, DeploymentModel originalModel) {
        var lexer = new DeploymentModelLexer(inputStream);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new DeploymentModelParser(tokenStream);
        
        var modelContext = parser.deploymentModel();
        
        var modelBuilder = (originalModel == null) ? new StandaloneDeploymentModelBuilder() : new DeltaDeploymentModelBuilder(originalModel);
        modelContext.accept(modelBuilder);
        
        return modelBuilder.getBuiltModel();
    }

}
