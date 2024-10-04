package gutta.prediction.span;

public abstract class SpanEvent {

    private final long timestamp;
    
    protected SpanEvent(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public long timestamp() {
        return this.timestamp;
    }

}
