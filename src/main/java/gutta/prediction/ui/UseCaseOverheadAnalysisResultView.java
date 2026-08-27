package gutta.prediction.ui;

import gutta.prediction.analysis.overhead.DurationChangeAnalysis.EffectSize;
import gutta.prediction.analysis.overhead.DurationChangeAnalysis.Result;

/**
 * View object to show the results from a use case invocation overhead analysis.
 */
record UseCaseOverheadAnalysisResultView(String useCaseName, double originalDuration, double newDuration, double cohensD, EffectSize effectSize,
        double oldAverageNumberOfRemoteCalls, double newAverageNumberOfRemoteCalls) implements Comparable<UseCaseOverheadAnalysisResultView> {

    public UseCaseOverheadAnalysisResultView(String useCaseName, Result result) {
        this(useCaseName, result.originalMean(), result.scenarioMean(), result.cohensD(), result.effectSize(), result.oldAverageNumberOfRemoteCalls(),
                result.newAverageNumberOfRemoteCalls());
    }

    @Override
    public int compareTo(UseCaseOverheadAnalysisResultView that) {
        return (this.useCaseName().compareTo(that.useCaseName()));
    }

}
