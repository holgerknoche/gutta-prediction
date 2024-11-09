package gutta.prediction.ui;

import gutta.prediction.analysis.overhead.DurationChangeAnalysis.Result;

record UseCaseOverheadAnalysisResultView(String useCaseName, double originalDuration, double newDuration, boolean significant, double pValue) implements Comparable<UseCaseOverheadAnalysisResultView> {
    
    public UseCaseOverheadAnalysisResultView(String useCaseName, Result result) {
        this(useCaseName, result.originalMean(), result.modifiedMean(), result.significantChange(), result.pValue());
    }
    
    @Override
    public int compareTo(UseCaseOverheadAnalysisResultView that) {
        return (this.useCaseName().compareTo(that.useCaseName()));
    }

}
