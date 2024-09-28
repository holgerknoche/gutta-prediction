package gutta.prediction.analysis.consistency;

import gutta.prediction.event.EntityWriteEvent;

import java.util.List;
import java.util.Set;

record ConsistencyAnalyzerResult(List<ConsistencyIssue<?>> issues, Set<EntityWriteEvent> committedWrites, Set<EntityWriteEvent> abortedWrites) {

}
