package gutta.prediction.ui;

import gutta.prediction.analysis.latency.DurationChangeAnalysis.Result;

record UseCaseLatencyAnalysisResultView(String useCaseName, double originalDuration, double newDuration, boolean significant, double pValue) implements Comparable<UseCaseLatencyAnalysisResultView> {
    
    public UseCaseLatencyAnalysisResultView(String useCaseName, Result result) {
        this(useCaseName, result.originalMean(), result.modifiedMean(), result.significantChange(), result.pValue());
    }
    
    @Override
    public int compareTo(UseCaseLatencyAnalysisResultView that) {
        return (this.useCaseName().compareTo(that.useCaseName()));
    }

}
