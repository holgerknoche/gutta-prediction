package gutta.prediction.ui;

record TraceOverheadAnalysisResultView(long traceId, double originalDuration, double newDuration, double changePercentage) implements Comparable<TraceOverheadAnalysisResultView> {

    @Override
    public int compareTo(TraceOverheadAnalysisResultView that) {
        return Long.compare(this.traceId(), that.traceId());
    }

}
