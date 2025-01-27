package gutta.prediction.ui;

import gutta.prediction.analysis.overhead.DurationChangeAnalysis.Result;

/**
 * View object to show the results from a use case invocation overhead analysis.
 */
record UseCaseOverheadAnalysisResultView(String useCaseName, double originalDuration, double newDuration, boolean significant, double pValue,
        double oldAverageNumberOfRemoteCals, double newAverageNumberOfRemoteCalls) implements Comparable<UseCaseOverheadAnalysisResultView> {

    public UseCaseOverheadAnalysisResultView(String useCaseName, Result result) {
        this(useCaseName, result.originalMean(), result.modifiedMean(), result.significantChange(), result.pValue(), result.oldAverageNumberOfRemoteCalls(),
                result.newAverageNumberOfRemoteCalls());
    }

    @Override
    public int compareTo(UseCaseOverheadAnalysisResultView that) {
        return (this.useCaseName().compareTo(that.useCaseName()));
    }

}
