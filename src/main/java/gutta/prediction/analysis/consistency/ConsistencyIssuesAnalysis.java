package gutta.prediction.analysis.consistency;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.rewriting.LatencyRewriter;
import gutta.prediction.rewriting.RewrittenEventTrace;
import gutta.prediction.rewriting.TransactionContextRewriter;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class ConsistencyIssuesAnalysis {

    public ConsistencyAnalysisResult analyzeTrace(EventTrace trace, DeploymentModel originalDeploymentModel, DeploymentModel modifiedDeploymentModel) {
        var originalTraceResult = this.analyzeTrace(trace, originalDeploymentModel);

        var rewrittenTrace = this.rewriteTrace(trace, modifiedDeploymentModel);
        var rewrittenTraceResult = this.analyzeTrace(rewrittenTrace, modifiedDeploymentModel);

        return this.diffAnalyzerResults(originalTraceResult, rewrittenTraceResult, rewrittenTrace::obtainOriginalEvent);
    }

    public ConsistencyAnalyzerResult analyzeTrace(EventTrace trace, DeploymentModel deploymentModel) {
        return new ConsistencyIssuesAnalyzer().analyzeTrace(trace, deploymentModel);
    }

    public RewrittenEventTrace rewriteTrace(EventTrace trace, DeploymentModel modifiedDeploymentModel) {
        var latencyRewriter = new LatencyRewriter(modifiedDeploymentModel);
        var transactionRewriter = new TransactionContextRewriter(modifiedDeploymentModel);
        return transactionRewriter.rewriteTrace(latencyRewriter.rewriteTrace(trace));
    }

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

        this.diffWrites(originalResult.committedWrites(), originalResult.abortedWrites(), rewrittenResult.committedWrites(), rewrittenResult.abortedWrites(),
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

    public interface EventMap {

        <T extends MonitoringEvent> T map(T event);

    }

    private interface IssueCollector {

        void collect(ConsistencyIssue<?> issue);

    }

    private interface WritesCollector {

        void collect(EntityWriteEvent event);

    }

}
