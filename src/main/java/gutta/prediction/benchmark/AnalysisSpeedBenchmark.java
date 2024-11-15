package gutta.prediction.benchmark;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.dsl.DeploymentModelReader;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.codec.EventTraceDecoder;
import org.apache.commons.math3.stat.StatUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

abstract class AnalysisSpeedBenchmark {

    protected void runBenchmark(String[] arguments) {
        var traceFileName = arguments[0];
        var deploymentModelFileName = arguments[1];
        var scenarioFileName = arguments[2];
        
        var warmupIterations = Integer.valueOf(arguments[3]);
        var timedIterations = Integer.valueOf(arguments[4]);
        
        this.runBenchmark(new File(traceFileName), new File(deploymentModelFileName), new File(scenarioFileName), warmupIterations, timedIterations);
    }
    
    private void runBenchmark(File traceFile, File deploymentModelFile, File scenarioFile, int warmupIterations, int timedIterations) {
        System.out.println("Running benchmark '" + this.getClass().getSimpleName() + "' with the following configuration:"); 
        System.out.println("Warmup iterations: " + warmupIterations);
        System.out.println("Timed iterations: " + timedIterations);
        System.out.println("Trace file: " + traceFile);
        System.out.println("Deployment model file: " + deploymentModelFile);
        System.out.println("Scenario file: " + scenarioFile);       
        
        System.out.println("Loading models...");
        var deploymentModel = this.loadDeploymentModel(deploymentModelFile);
        var scenarioModel = this.loadScenarioModel(scenarioFile, deploymentModel);
        
        System.out.println("Loading traces...");
        var traces = this.loadTraces(traceFile);
        
        System.out.println("Running warmup iterations...");
        for (var iteration = 0; iteration < warmupIterations; iteration++) {
            this.runAnalysis(traces, deploymentModel, scenarioModel);
        }
        
        System.out.println("Running timed iterations...");
        var durations = new double[timedIterations];
        for (var iteration = 0; iteration < timedIterations; iteration++) {
            var timeBefore = System.nanoTime();
            this.runAnalysis(traces, deploymentModel, scenarioModel);
            var timeAfter = System.nanoTime();
            
            var durationMs = TimeUnit.NANOSECONDS.toMillis(timeAfter - timeBefore);
            durations[iteration] = durationMs;
                        
            System.out.println("#" + iteration + ": " + durationMs + "ms");
        }
        
        System.out.println("Average duration: " + StatUtils.mean(durations));
        System.out.print("Standard deviation: " + Math.sqrt(StatUtils.variance(durations)));
    }    
    
    private DeploymentModel loadDeploymentModel(File deploymentModelFile) {
        try (var inputStream = new FileInputStream(deploymentModelFile)) {
            return new DeploymentModelReader().readModel(inputStream);
        } catch (IOException e) {
            throw new BenchmarkException("Error reading the deployment model from '" + deploymentModelFile + "'.", e);
        }
    }
    
    private DeploymentModel loadScenarioModel(File scenarioFile, DeploymentModel referenceModel) {
        try (var inputStream = new FileInputStream(scenarioFile)) {
            return new DeploymentModelReader().readModel(inputStream, referenceModel);
        } catch (IOException e) {
            throw new BenchmarkException("Error reading the scenario model from '" + scenarioFile + "'.", e);
        }
    }
    
    private Collection<EventTrace> loadTraces(File traceFile) {
        try (var inputStream = new FileInputStream(traceFile)) {
            return new EventTraceDecoder().decodeTraces(inputStream);
        } catch (IOException e) {
            throw new BenchmarkException("Error reading the traces from '" + traceFile + "'.", e);
        }
    }
    
    protected abstract void runAnalysis(Collection<EventTrace> traces, DeploymentModel deploymentModel, DeploymentModel scenarioModel);
        
    private static class BenchmarkException extends RuntimeException {
        
        private static final long serialVersionUID = 561792470894726533L;

        public BenchmarkException(String message, Throwable cause) {
            super(message, cause);
        }
        
    }
    
}
