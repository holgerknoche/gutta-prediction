package gutta.prediction.span;

import gutta.prediction.util.EqualityUtil;

public abstract class Interval {

    private final long startTimestamp;

    private long endTimestamp;
    
    protected Interval(long startTimestamp, long endTimestamp) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }

    public long startTimestamp() {
        return this.startTimestamp;
    }

    public long endTimestamp() {
        return this.endTimestamp;
    }

    protected void endTimestamp(long value) {
        this.endTimestamp = value;
    }
    
    @Override
    public int hashCode() {
        return (int) (this.startTimestamp + this.endTimestamp);
    }
    
    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    protected boolean equalsInternal(Interval that) {
        return (this.startTimestamp == that.startTimestamp) &&
                (this.endTimestamp == that.endTimestamp);
    }

}
