package gutta.prediction.span;

import gutta.prediction.util.EqualityUtil;

public abstract class SpanOverlay extends Interval implements TraceElement {
    
    protected SpanOverlay(long startTimestamp) {
        this(startTimestamp, 0);
    }

    protected SpanOverlay(long startTimestamp, long endTimestamp) {
        super(startTimestamp, endTimestamp);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    protected boolean equalsInternal(SpanOverlay that) {
        return super.equalsInternal(that);
    }
    
    @Override
    public void traverse(TraceElementVisitor<?> visitor) {
        this.accept(visitor);
    }

}
