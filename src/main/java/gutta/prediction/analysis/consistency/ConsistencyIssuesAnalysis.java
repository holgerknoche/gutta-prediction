package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.rewriting.OverheadRewriter;
import gutta.prediction.rewriting.RewrittenEventTrace;
import gutta.prediction.rewriting.TransactionContextRewriter;
import gutta.prediction.util.SimpleTaskScope;
import gutta.prediction.util.SimpleTaskScope.Subtask;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static java.util.Objects.requireNonNull;

/**
 * A {@link ConsistencyIssuesAnalysis} performs a consistency analysis of a trace with respect to a scenario. For this purpose, the given trace is rewritten,
 * both traces are analyzed, and the results are compared to each other.
 */
public class ConsistencyIssuesAnalysis {

    private final CheckCrossComponentAccesses checkCrossComponentAccesses;

    private final CheckInterleavingAccesses checkInterleavingAccesses;

    /**
     * Creates a new analysis with default parameters.
     */
    public ConsistencyIssuesAnalysis() {
        this(CheckCrossComponentAccesses.YES, CheckInterleavingAccesses.YES);
    }

    /**
     * Creates a new analysis with the given parameters.
     * 
     * @param checkCrossComponentAccesses Denotes whether to check for cross-component accesses
     * @param checkInterleavingAccesses   Denotes whether to check for interleaving entity accesses
     */
    public ConsistencyIssuesAnalysis(CheckCrossComponentAccesses checkCrossComponentAccesses, CheckInterleavingAccesses checkInterleavingAccesses) {
        this.checkCrossComponentAccesses = checkCrossComponentAccesses;
        this.checkInterleavingAccesses = checkInterleavingAccesses;
    }

    /**
     * Analyzes the given traces with respect to the given scenario.
     * 
     * @param traces          The traces to analyze
     * @param deploymentModel The deployment model of the given trace
     * @param scenarioModel   The scenario model based on the given deployment model
     * @return The result of the analysis
     */
    public Map<EventTrace, ConsistencyAnalysisResult> analyzeTraces(Collection<EventTrace> traces, DeploymentModel deploymentModel,
            DeploymentModel scenarioModel) {

        try (var scope = new SimpleTaskScope<ConsistencyAnalysisResult>()) {
            var traceToTask = new HashMap<EventTrace, Subtask<ConsistencyAnalysisResult>>(traces.size());

            // Schedule the analyses for execution
            for (var trace : traces) {
                var task = scope.fork(() -> this.analyzeTrace(trace, deploymentModel, scenarioModel));
                traceToTask.put(trace, task);
            }

            // Run the analyses, throwing an exception if one of the exceptions failed
            scope.join().throwIfFailed();

            var traceToResult = new HashMap<EventTrace, ConsistencyAnalysisResult>(traces.size());
            traceToTask.forEach((trace, task) -> traceToResult.put(trace, task.get()));

            return traceToResult;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConsistencyAnalysisException("Unexpected interrupt while waiting for the analysis results.", e);
        } catch (ExecutionException e) {
            throw new ConsistencyAnalysisException("Execution exception during the analysis.", e);
        }
    }

    /**
     * Analyzes the given trace with respect to the given scenario.
     * 
     * @param trace           The trace to analyze
     * @param deploymentModel The deployment model of the given trace
     * @param scenarioModel   The scenario model based on the given deployment model
     * @return The result of the analysis
     */
    public ConsistencyAnalysisResult analyzeTrace(EventTrace trace, DeploymentModel deploymentModel, DeploymentModel scenarioModel) {
        var originalTraceResult = this.analyzeTrace(trace, deploymentModel);

        var rewrittenTrace = this.rewriteTrace(trace, scenarioModel);
        var rewrittenTraceResult = this.analyzeTrace(rewrittenTrace, scenarioModel);

        return this.diffAnalyzerResults(originalTraceResult, rewrittenTraceResult, rewrittenTrace::obtainOriginalEvent);
    }

    /**
     * Analyzes the given trace.
     * 
     * @param trace           The trace to analyze
     * @param deploymentModel The deployment model of the given trace
     * @return The result of the analysis
     */
    public ConsistencyAnalyzerResult analyzeTrace(EventTrace trace, DeploymentModel deploymentModel) {
        return new ConsistencyIssuesAnalyzer(this.checkCrossComponentAccesses, this.checkInterleavingAccesses).analyzeTrace(trace, deploymentModel);
    }

    /**
     * Rewrites the given trace to match the given scenario model.
     * 
     * @param trace         The trace to rewrite
     * @param scenarioModel The scenario model to rewrite to
     * @return The rewritten trace
     */
    public RewrittenEventTrace rewriteTrace(EventTrace trace, DeploymentModel scenarioModel) {
        var overheadRewriter = new OverheadRewriter(scenarioModel);
        var transactionRewriter = new TransactionContextRewriter(scenarioModel);
        return transactionRewriter.rewriteTrace(overheadRewriter.rewriteTrace(trace));
    }

    /**
     * Determines the difference between the given results, using the given event map.
     * 
     * @param originalResult  The result from the original trace
     * @param rewrittenResult The result from the rewritten trace
     * @param eventMap        The event map from the rewritten events to the original events
     * @return The difference of the results
     */
    public ConsistencyAnalysisResult diffAnalyzerResults(ConsistencyAnalyzerResult originalResult, ConsistencyAnalyzerResult rewrittenResult,
            EventMap eventMap) {
        var newIssues = new HashSet<ConsistencyIssue<?>>();
        var obsoleteIssues = new HashSet<ConsistencyIssue<?>>();
        var unchangedIssues = new HashSet<ConsistencyIssue<?>>();

        this.diffIssues(originalResult.issues(), rewrittenResult.issues(), eventMap, newIssues::add, obsoleteIssues::add, unchangedIssues::add);

        var nowCommittedWrites = new HashSet<EntityWriteEvent>();
        var nowRevertedWrites = new HashSet<EntityWriteEvent>();
        var unchangedCommittedWrites = new HashSet<EntityWriteEvent>();
        var unchangedRevertedWrites = new HashSet<EntityWriteEvent>();

        this.diffWrites(originalResult.committedWrites(), originalResult.revertedWrites(), rewrittenResult.committedWrites(), rewrittenResult.revertedWrites(),
                eventMap, nowCommittedWrites::add, nowRevertedWrites::add, unchangedCommittedWrites::add, unchangedRevertedWrites::add);

        return new ConsistencyAnalysisResult(originalResult.issues().size(), rewrittenResult.issues().size(), newIssues, obsoleteIssues, unchangedIssues,
                nowCommittedWrites, nowRevertedWrites, unchangedCommittedWrites, unchangedRevertedWrites);
    }

    private void diffIssues(Set<ConsistencyIssue<?>> theseIssues, Set<ConsistencyIssue<?>> thoseIssues, EventMap eventMap, IssueCollector newIssuesCollector,
            IssueCollector missingIssuesCollector, IssueCollector unchangedIssuesCollector) {
        var matchingIssues = new HashSet<ConsistencyIssue<?>>(theseIssues.size());

        // We have to start at "those issues", since the map maps them back to "these issues"
        for (var issue : thoseIssues) {
            var mappedEvent = eventMap.map(issue.event());
            if (mappedEvent == null) {
                // All events should be mapped, as we do not add or remove events
                throw new IllegalStateException("Unmapped event '" + issue.event() + "'.");
            }

            var mappedIssue = issue.rewriteToEvent(mappedEvent);
            if (theseIssues.contains(mappedIssue)) {
                // If the mapped issue exists in the this result, we have a match
                matchingIssues.add(mappedIssue);
            } else {
                // If the issue does not exist in the this result, consider it new
                newIssuesCollector.collect(issue);
            }
        }

        for (var issue : theseIssues) {
            if (!matchingIssues.contains(issue)) {
                missingIssuesCollector.collect(issue);
            }
        }

        matchingIssues.forEach(unchangedIssuesCollector::collect);
    }

    private void diffWrites(Set<EntityWriteEvent> theseCommittedWrites, Set<EntityWriteEvent> theseRevertedWrites, Set<EntityWriteEvent> thoseCommittedWrites,
            Set<EntityWriteEvent> thoseRevertedWrites, EventMap eventMap, WritesCollector nowCommittedWritesCollector,
            WritesCollector nowRevertedWritesCollector, WritesCollector unchangedCommittedWritesCollector, WritesCollector unchangedRevertedWritesCollector) {

        var matchingCommittedWrites = new HashSet<EntityWriteEvent>();
        var matchingRevertedWrites = new HashSet<EntityWriteEvent>();

        for (var write : thoseCommittedWrites) {
            var mappedWrite = requireNonNull(eventMap.map(write));

            if (theseCommittedWrites.contains(mappedWrite)) {
                // If the write is committed in both results, record a match
                matchingCommittedWrites.add(mappedWrite);
            } else if (theseRevertedWrites.contains(mappedWrite)) {
                // If the write used to be reverted, record it as "now committed"
                nowCommittedWritesCollector.collect(write);
            } else {
                // Otherwise, the write is "lost", which should not happen
                throw new IllegalStateException("Undefined state of write '" + write + "'.");
            }
        }

        for (var write : thoseRevertedWrites) {
            var mappedWrite = requireNonNull(eventMap.map(write));

            if (theseRevertedWrites.contains(mappedWrite)) {
                // If the write is reverted in both results, record a match
                matchingRevertedWrites.add(mappedWrite);
            } else if (theseCommittedWrites.contains(mappedWrite)) {
                // If the write used to be committed, record it as "now reverted"
                nowRevertedWritesCollector.collect(write);
            } else {
                // Otherwise, the write is "lost", which should not happen
                throw new IllegalStateException("Undefined state of write '" + write + "'.");
            }
        }

        matchingCommittedWrites.forEach(unchangedCommittedWritesCollector::collect);
        matchingRevertedWrites.forEach(unchangedRevertedWritesCollector::collect);
    }

    /**
     * An event map, e.g., mapping rewritten events to their original events.
     */
    public interface EventMap {

        /**
         * Maps the given event.
         * 
         * @param <T>   The type of event
         * @param event The event to map
         * @return The mapped event
         */
        <T extends MonitoringEvent> T map(T event);

    }

    private interface IssueCollector {

        void collect(ConsistencyIssue<?> issue);

    }

    private interface WritesCollector {

        void collect(EntityWriteEvent event);

    }

    /**
     * This exception is thrown if an error occurs during the consistency analysis.
     */
    static class ConsistencyAnalysisException extends RuntimeException {

        private static final long serialVersionUID = -8800408324143943481L;

        public ConsistencyAnalysisException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}
