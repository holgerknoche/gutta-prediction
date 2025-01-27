package gutta.prediction.dsl;

import gutta.prediction.domain.DeploymentModel;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * A {@link DeploymentModelReader} provides convenience functions for reading a textual specification of a deployment model from various sources.
 */
public class DeploymentModelReader {

    /**
     * Reads a standalone deployment model from a given input stream.
     * 
     * @param inputStream The input stream to read from
     * @return The deployment model
     * @throws IOException If an I/O exception occurs while reading the model
     */
    public DeploymentModel readModel(InputStream inputStream) throws IOException {
        return this.readModel(CharStreams.fromStream(inputStream, StandardCharsets.UTF_8), null);
    }

    /**
     * Reads a delta deployment model from a given input stream based on the given deployment model.
     * 
     * @param inputStream   The input stream to read from
     * @param originalModel The original model to base the delta on
     * @return The resulting deployment model
     * @throws IOException If an I/O exception occurs while reading the model
     */
    public DeploymentModel readModel(InputStream inputStream, DeploymentModel originalModel) throws IOException {
        return this.readModel(CharStreams.fromStream(inputStream, StandardCharsets.UTF_8), originalModel);
    }

    /**
     * Reads a standalone deployment model from a given string.
     * 
     * @param input The string to read from
     * @return The deployment model
     * @throws IOException If an I/O exception occurs while reading the model
     */
    public DeploymentModel readModel(String input) {
        return this.readModel(CharStreams.fromString(input), null);
    }

    /**
     * Reads a delta deployment model from a given string based on the given deployment model.
     * 
     * @param input         The string to read from
     * @param originalModel The original model to base the delta on
     * @return The resulting deployment model
     * @throws IOException If an I/O exception occurs while reading the model
     */
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
