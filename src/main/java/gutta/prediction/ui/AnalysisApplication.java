package gutta.prediction.ui;

import java.io.File;

/**
 * Main entry point for the analysis application.
 */
public class AnalysisApplication {
    
    /**
     * Starts the analysis application from the command line.
     * 
     * @param args The command line arguments passed from the invocation
     */
    public static void main(String[] args) {
        var arguments = parseArguments(args);
        
        var overviewFrame = new UseCaseOverviewFrame(arguments.tracesFile(), arguments.deploymentModelFile());
        overviewFrame.setVisible(true);
    }
    
    private static Arguments parseArguments(String[] arguments) {
        return switch (arguments.length) {
        case 0 -> new Arguments(null, null);            
        case 1 -> new Arguments(new File(arguments[0]), null);
        default -> new Arguments(new File(arguments[0]), new File(arguments[1]));
        };
    }
    
    private record Arguments(File tracesFile, File deploymentModelFile) {}

}
