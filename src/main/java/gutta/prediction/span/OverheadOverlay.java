package gutta.prediction.span;

public class OverheadOverlay extends SpanOverlay {

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
