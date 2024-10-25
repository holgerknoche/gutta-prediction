package gutta.prediction.ui;

public record UseCaseConsistencyAnalysisResultView(String useCaseName, int numberOfTracesWithChangeInIssues, double percentageWithChangeInIssues, int numberOfTracesWithChangeInWrites, double percentageWithChangeInWrites) implements Comparable<UseCaseConsistencyAnalysisResultView> {
    
    @Override
    public int compareTo(UseCaseConsistencyAnalysisResultView that) {
        return (this.useCaseName().compareTo(that.useCaseName()));
    }

}
