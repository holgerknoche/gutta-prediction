package gutta.prediction.span;

public record Trace(long id, String name, RootSpan rootSpan) {

    private static final Trace EMPTY_TRACE = buildEmptyTrace();

    private static Trace buildEmptyTrace() {
        return new Trace(0, "", new RootSpan(0));
    }

    public static Trace emptyTrace() {
        return EMPTY_TRACE;
    }

}
