package gutta.prediction.span;

/**
 * An {@link OverheadOverlay} is a span overlay that represents invocation overhead.
 */
public class OverheadOverlay extends SpanOverlay {

    /**
     * Creates a new overlay with the given start and end timestamps.
     * 
     * @param startTimestamp The start timestamp of the overlay
     * @param endTimestamp   The end timestamp of the overlay
     */
    public OverheadOverlay(long startTimestamp, long endTimestamp) {
        super(startTimestamp, endTimestamp);
    }

    @Override
    public <R> R accept(TraceElementVisitor<R> visitor) {
        return visitor.handleOverheadOverlay(this);
    }

    @Override
    public String toString() {
        return "Overhead: " + this.startTimestamp() + " -- " + this.endTimestamp();
    }

}
