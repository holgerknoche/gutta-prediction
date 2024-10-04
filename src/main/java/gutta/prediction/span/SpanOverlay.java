package gutta.prediction.span;

public abstract class SpanOverlay extends Interval {

    protected SpanOverlay(long startTimestamp, long endTimestamp) {
        super(startTimestamp, endTimestamp);
    }

}
