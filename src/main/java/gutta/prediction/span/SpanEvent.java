package gutta.prediction.span;

import gutta.prediction.util.EqualityUtil;

public abstract class SpanEvent implements TraceElement {

    private final long timestamp;
    
    protected SpanEvent(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public long timestamp() {
        return this.timestamp;
    }
    
    @Override
    public int hashCode() {
        return (int) this.timestamp();
    }
    
    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    protected boolean equalsInternal(SpanEvent that) {
        return (this.timestamp() == that.timestamp());
    }

}
