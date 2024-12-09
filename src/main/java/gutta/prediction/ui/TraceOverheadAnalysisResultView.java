package gutta.prediction.ui;

import static gutta.prediction.ui.PercentageUtil.*;

/**
 * View object to show the results from an invocation overhead analysis.
 */
record TraceOverheadAnalysisResultView(long traceId, double originalDuration, double newDuration, double oldNumberOfRemoteCalls, double newNumberOfRemoteCalls)
        implements Comparable<TraceOverheadAnalysisResultView> {

    public double durationChangePercentage() {
        return calculateChangePercentage(this.originalDuration(), this.newDuration());
    }

    public double remoteCallsChangePercentage() {
        return calculateChangePercentage(this.oldNumberOfRemoteCalls(), this.newNumberOfRemoteCalls());
    }

    @Override
    public int compareTo(TraceOverheadAnalysisResultView that) {
        return Long.compare(this.traceId(), that.traceId());
    }

}
