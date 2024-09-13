package gutta.prediction.span;

public abstract class Interval {

    private final long beginNs;

    private long endNs;

    protected Interval(long beginNs) {
        this.beginNs = beginNs;
    }

    public long beginNs() {
        return this.beginNs;
    }

    public long endNs() {
        return this.endNs;
    }

    protected void endNs(long value) {
        this.endNs = value;
    }

}
