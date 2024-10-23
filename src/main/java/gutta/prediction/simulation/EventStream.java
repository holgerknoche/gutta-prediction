package gutta.prediction.simulation;

import gutta.prediction.event.EventTrace;
import gutta.prediction.event.MonitoringEvent;

import java.util.List;
import java.util.function.Consumer;

public class EventStream {

    private final List<MonitoringEvent> events;

    private int maxPosition;

    private int currentPosition;

    public EventStream(EventTrace trace) {
        this(trace.events());
    }
    
    public EventStream(List<MonitoringEvent> events) {
        this.events = events;
        this.maxPosition = (events.size() - 1);
    }

    public int size() {
        return this.events.size();
    }

    public MonitoringEvent lookahead(int amount) {        
        var desiredPosition = (this.currentPosition + amount);
        if (desiredPosition > this.maxPosition || desiredPosition < 0) {
            return null;
        }

        return this.events.get(desiredPosition);
    }
    
    public MonitoringEvent lookback(int amount) {
        return this.lookahead(-amount);
    }

    public void consume() {
        if (this.currentPosition <= this.maxPosition) {
            this.currentPosition++;
        }
    }
    
    public void forEach(Consumer<MonitoringEvent> action) {
        while (true) {
            var currentEvent = this.lookahead(0);
            if (currentEvent == null) {
                return;
            }
            
            action.accept(currentEvent);
            this.consume();
        }
    }
    
}
