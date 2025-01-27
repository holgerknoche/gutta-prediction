package gutta.prediction.analysis.consistency;

import gutta.prediction.event.EntityWriteEvent;

import java.util.Set;

/**
 * This record represents the result of a {@link ConsistencyIssuesAnalysis} of two corresponding event traces.
 * 
 * @param numberOfIssuesInOriginalTrace  The number of consistency issues in the original trace
 * @param numberOfIssuesInRewrittenTrace The number of consistency issues in the rewritten trace
 * @param newIssues                      The set of issues that occur in the rewritten trace, but not in the original trace
 * @param obsoleteIssues                 The set of issues that occur in the original trace, but not in the rewritten trace
 * @param unchanged                      Issues The set of issues that occur both in the original and the rewritten trace
 * @param nowCommittedWrites             The set of writes that are committed in the rewritten trace, but reverted in the original trace
 * @param nowRevertedWrites              The set of writes that are reverted in the rewritten trace, but committed in the original trace
 * @param unchangedCommittedWrites       The set of writes that are committed in both the rewritten and the original trace
 * @param unchangedRevertedWrites        The set of writes that are reverted in both the rewritten and the original trace
 */
public record ConsistencyAnalysisResult(int numberOfIssuesInOriginalTrace, int numberOfIssuesInRewrittenTrace, Set<ConsistencyIssue<?>> newIssues,
        Set<ConsistencyIssue<?>> obsoleteIssues, Set<ConsistencyIssue<?>> unchangedIssues, Set<EntityWriteEvent> nowCommittedWrites,
        Set<EntityWriteEvent> nowRevertedWrites, Set<EntityWriteEvent> unchangedCommittedWrites, Set<EntityWriteEvent> unchangedRevertedWrites) {

}
