package gutta.prediction.ui;

import java.io.File;

public class AnalysisApplication {
    
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
