package gutta.prediction.span;

public record Trace(long id, String name, Span rootSpan) implements TraceElement {
    
    public long startTimestamp() {
        return this.rootSpan.startTimestamp();
    }
    
    public long endTimestamp() {
        return this.rootSpan.endTimestamp();
    }
    
    @Override
    public <R> R accept(TraceElementVisitor<R> visitor) {
        return visitor.handleTrace(this);
    }
    
    @Override
    public void traverse(TraceElementVisitor<?> visitor) {
        visitor.handleTrace(this);
        
        this.rootSpan().traverse(visitor);
    }

}
