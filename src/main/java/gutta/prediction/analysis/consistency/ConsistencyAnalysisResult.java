package gutta.prediction.analysis.consistency;

import gutta.prediction.event.EntityWriteEvent;

import java.util.Set;

public record ConsistencyAnalysisResult(int numberOfIssuesInOriginalTrace, int numberOfIssuesInModifiedTrace, Set<ConsistencyIssue<?>> newIssues, Set<ConsistencyIssue<?>> obsoleteIssues, Set<ConsistencyIssue<?>> unchangedIssues, Set<EntityWriteEvent> nowCommittedWrites, Set<EntityWriteEvent> nowRevertedWrites) {

}
