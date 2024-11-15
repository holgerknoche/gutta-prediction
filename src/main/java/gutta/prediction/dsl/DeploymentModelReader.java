package gutta.prediction.dsl;

import gutta.prediction.domain.DeploymentModel;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class DeploymentModelReader {
    
    public DeploymentModel readModel(InputStream inputStream) throws IOException {
        return this.readModel(CharStreams.fromStream(inputStream, StandardCharsets.UTF_8), null);
    }
    
    public DeploymentModel readModel(InputStream inputStream, DeploymentModel originalModel) throws IOException {
        return this.readModel(CharStreams.fromStream(inputStream, StandardCharsets.UTF_8), originalModel);
    }
    
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
