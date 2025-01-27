package gutta.prediction.ui;

/**
 * View object to show the results from a use case consistency analysis.
 */
public record UseCaseConsistencyAnalysisResultView(String useCaseName, int numberOfTracesWithChangeInIssues, double percentageWithChangeInIssues,
        int numberOfTracesWithChangeInWrites, double percentageWithChangeInWrites) implements Comparable<UseCaseConsistencyAnalysisResultView> {

    @Override
    public int compareTo(UseCaseConsistencyAnalysisResultView that) {
        return (this.useCaseName().compareTo(that.useCaseName()));
    }

}
