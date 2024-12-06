package gutta.prediction.span;

/**
 * A {@link Trace} represents a trace similar to that of OpenTelemetry, i.e., based on {@linkplain Span spans}.
 * 
 * @param id       The id of the trace
 * @param name     The name to show for the trace
 * @param rootSpan The root span of the trace from which other spans may be invoked
 */
public record Trace(long id, String name, Span rootSpan) implements TraceElement {

    /**
     * Returns the start timestamp of the trace.
     * 
     * @return see above
     */
    public long startTimestamp() {
        return this.rootSpan.startTimestamp();
    }

    /**
     * Returns the end timestamp of the trace.
     * 
     * @return see above
     */
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
