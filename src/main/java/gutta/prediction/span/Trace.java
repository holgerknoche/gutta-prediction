package gutta.prediction.span;

public record Trace(String id, Span rootSpan) {

    private static final Trace EMPTY_TRACE = buildEmptyTrace();

    private static Trace buildEmptyTrace() {
        return new Trace("", new RootSpan(0));
    }

    public static Trace emptyTrace() {
        return EMPTY_TRACE;
    }

}
