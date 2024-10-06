package gutta.prediction.span;

public class LatencyOverlay extends SpanOverlay {

    public LatencyOverlay(long startTimestamp, long endTimestamp) {
        super(startTimestamp, endTimestamp);
    }
    
    @Override
    public <R> R accept(TraceElementVisitor<R> visitor) {
        return visitor.handleLatencyOverlay(this);
    }
    
    @Override
    public String toString() {
        return "Latency: " + this.startTimestamp() + " -- " + this.endTimestamp();
    }

}
