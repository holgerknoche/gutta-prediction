package gutta.prediction.benchmark;

import gutta.prediction.analysis.consistency.CheckCrossComponentAccesses;
import gutta.prediction.analysis.consistency.CheckInterleavingAccesses;
import gutta.prediction.analysis.consistency.ConsistencyIssuesAnalysis;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

/**
 * A benchmark that measures the speed of the consistency analysis using given data.
 */
public class ConsistencyAnalysisSpeedBenchmark extends AnalysisSpeedBenchmark {

    /**
     * Runs the benchmark.
     * 
     * @param arguments The command line arguments of the benchmark
     */
    public static void main(String[] arguments) {
        new ConsistencyAnalysisSpeedBenchmark().runBenchmark(arguments);
    }

    @Override
    protected void runAnalysis(Collection<EventTrace> traces, DeploymentModel deploymentModel, DeploymentModel scenarioModel) {
        var executorService = Executors.newCachedThreadPool(this::createDaemonThread);     
        var latch = new CountDownLatch(traces.size());        
        
        for (var trace : traces) {
            var analysis = new ConsistencyIssuesAnalysis(CheckCrossComponentAccesses.YES, CheckInterleavingAccesses.YES); 
            
            executorService.submit(() -> {
                analysis.analyzeTrace(trace, deploymentModel, scenarioModel);
                latch.countDown();
            });
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("Unexpected interrupt while waiting for the analysis results.", e);
        }
    }
    
    private Thread createDaemonThread(Runnable task) {
        var thread = new Thread(task);
        thread.setDaemon(true);
        
        return thread;
    }

}
