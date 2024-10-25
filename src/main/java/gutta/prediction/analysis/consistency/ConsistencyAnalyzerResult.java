package gutta.prediction.analysis.consistency;

import gutta.prediction.event.EntityWriteEvent;

import java.util.Set;

public record ConsistencyAnalyzerResult(Set<ConsistencyIssue<?>> issues, Set<EntityWriteEvent> committedWrites, Set<EntityWriteEvent> abortedWrites) {
    
}
