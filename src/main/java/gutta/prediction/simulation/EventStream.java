package gutta.prediction.simulation;

import gutta.prediction.event.EventTrace;
import gutta.prediction.event.MonitoringEvent;

import java.util.List;
import java.util.function.Consumer;

/**
 * An {@link EventStream} is an iteration construct on {@linkplain EventTrace event traces} that allows to look ahead or look back from the current event.
 */
public class EventStream {

    private final List<MonitoringEvent> events;

    private int maxPosition;

    private int currentPosition;

    /**
     * Creates a new event stream from the given trace.
     * 
     * @param trace The trace to create the stream from
     */
    public EventStream(EventTrace trace) {
        this(trace.events());
    }
    
    private EventStream(List<MonitoringEvent> events) {
        this.events = events;
        this.maxPosition = (events.size() - 1);
    }

    /**
     * Returns the number of events in this stream.
     * 
     * @return see above
     */
    public int size() {
        return this.events.size();
    }

    /***
     * Looks ahead the given amount from the current element and returns the respective event.  
     * 
     * @param amount The amount to look ahead. 0 returns the current object, 1 the immediate successor.
     * @return The respective event or {@code null} if the addressed event is past the boundaries of the stream
     */
    public MonitoringEvent lookahead(int amount) {        
        var desiredPosition = (this.currentPosition + amount);
        if (desiredPosition > this.maxPosition || desiredPosition < 0) {
            return null;
        }

        return this.events.get(desiredPosition);
    }
    
    /**
     * Consumes the current event, i.e., moves to the next element if the stream.
     */
    public void consume() {
        if (this.currentPosition <= this.maxPosition) {
            this.currentPosition++;
        }
    }
    
    /**
     * Performs the given action for each remaining element in the stream.
     * 
     * @param action The action to perform
     */
    public void forEachRemaining(Consumer<MonitoringEvent> action) {
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
