package gutta.prediction.ui;

record TraceLatencyAnalysisResultView(long traceId, double originalDuration, double newDuration, double changePercentage) implements Comparable<TraceLatencyAnalysisResultView> {

    @Override
    public int compareTo(TraceLatencyAnalysisResultView that) {
        return Long.compare(this.traceId(), that.traceId());
    }

}
