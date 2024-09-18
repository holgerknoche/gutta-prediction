package gutta.prediction.span;

import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.MonitoringEventVisitor;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import gutta.prediction.stream.EventStream;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class TraceBuilder {
    
    public Trace buildTrace(List<MonitoringEvent> events) {
        return new TraceBuilderWorker(events).buildTrace();
    }
    
    private static class TraceBuilderWorker implements MonitoringEventVisitor<Void> {
     
        private final EventStream events;
        
        private String useCaseName;
        
        private long traceId;
        
        private Deque<Span> stack;
        
        public TraceBuilderWorker(List<MonitoringEvent> events) {
            this.events = new EventStream(events);
        }
        
        public Trace buildTrace() {
            this.stack = new ArrayDeque<>();
            this.useCaseName = null;
            this.traceId = 0;
            
            while (true) {
                var currentEvent = this.events.lookahead(0);
                if (currentEvent == null) {
                    break;
                }
                
                currentEvent.accept(this);
                this.events.consume();
            }
            
            // TODO Actually build a trace
            return new Trace(this.traceId, this.useCaseName, null);
        }
        
        @Override
        public Void handleUseCaseStartEvent(UseCaseStartEvent event) {
            this.useCaseName = event.name();
            this.traceId = event.traceId();
            
            return null;
        }
        
        @Override
        public Void handleUseCaseEndEvent(UseCaseEndEvent event) {
            // Do nothing
            return null;
        }
        
    }

}
