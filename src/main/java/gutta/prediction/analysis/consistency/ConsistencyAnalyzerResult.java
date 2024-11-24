package gutta.prediction.analysis.consistency;

import gutta.prediction.event.EntityWriteEvent;

import java.util.Set;

/**
 * This record represents the result of a {@link ConsistencyIssuesAnalyzer} of an event trace.
 * 
 * @param issues          The set of found issues in the trace
 * @param committedWrites The set of writes that were committed in the trace
 * @param revertedWrites  The set of writes that were reverted in the trace
 */
public record ConsistencyAnalyzerResult(Set<ConsistencyIssue<?>> issues, Set<EntityWriteEvent> committedWrites, Set<EntityWriteEvent> revertedWrites) {

}
