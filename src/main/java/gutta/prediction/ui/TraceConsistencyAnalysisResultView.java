package gutta.prediction.ui;

/**
 * View object to show the results from a trace consistency analysis.
 */
record TraceConsistencyAnalysisResultView(long traceId, boolean issuesChanged, int numberOfChangedIssues, int numberOfUnchangedIssues, boolean writesChanged,
        int numberOfChangedWrites, int numberOfUnchangedWrites) implements Comparable<TraceConsistencyAnalysisResultView> {

    @Override
    public int compareTo(TraceConsistencyAnalysisResultView that) {
        return Long.compare(this.traceId(), that.traceId());
    }

}
