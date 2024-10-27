package gutta.prediction.ui;

import gutta.prediction.analysis.consistency.ConsistencyIssuesAnalysis;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.TableModel;

class UseCaseConsistencyAnalysisFrame extends UseCaseAnalysisFrameTemplate<UseCaseConsistencyAnalysisResultView> {

    private static final long serialVersionUID = 887307194797918432L;

    public UseCaseConsistencyAnalysisFrame(Map<String, Collection<EventTrace>> tracesPerUseCase, String originalDeploymentModelSpec, DeploymentModel originalDeploymentModel) {
        super(tracesPerUseCase, originalDeploymentModelSpec, originalDeploymentModel);
        
        this.initialize();
        this.initializeControls();
        this.initializeDefaults();
    }
    
    private void initialize() {
        super.initialize("Consistency Change Analysis");
    }
    
    @Override
    protected void onRowSelection(JTable table, int rowIndex) {
        var useCaseName = (String) table.getValueAt(rowIndex, 0);
        var traces = this.tracesPerUseCase.get(useCaseName);
        
        if (traces != null) {
            var frame = new TracesForUseCaseConsistencyAnalysisFrame(useCaseName, traces, this.originalDeploymentModelSpec(), this.originalDeploymentModel(), this.modifiedDeploymentModelSpec());
            frame.setVisible(true);
        }
    }

    @Override
    protected UseCaseConsistencyAnalysisResultView analyzeScenario(String useCaseName, Collection<EventTrace> traces, DeploymentModel originalDeploymentModel,
            DeploymentModel modifiedDeploymentModel) {
        
        var numberOfTracesWithChangeInIssues = 0;
        var numberOfTracesWithChangeInWrites = 0;
        
        for (var trace : traces) {
            var traceResult = new ConsistencyIssuesAnalysis().analyzeTrace(trace, originalDeploymentModel, modifiedDeploymentModel);
            
            if (!traceResult.newIssues().isEmpty() || !traceResult.obsoleteIssues().isEmpty()) {
                numberOfTracesWithChangeInIssues++;
            }
            if (!traceResult.nowCommittedWrites().isEmpty() || !traceResult.nowRevertedWrites().isEmpty()) {
                numberOfTracesWithChangeInWrites++;
            }                        
        }
        
        var numberOfTraces = traces.size();
        var percentageWithChangeInIssues = (double) numberOfTracesWithChangeInIssues / (double) numberOfTraces;
        var percentageWithChangeInWrites = (double) numberOfTracesWithChangeInWrites / (double) numberOfTraces;
        
        return new UseCaseConsistencyAnalysisResultView(useCaseName, numberOfTracesWithChangeInIssues, percentageWithChangeInIssues, numberOfTracesWithChangeInWrites, percentageWithChangeInWrites);
    }

    @Override
    protected TableModel createTableModel(List<UseCaseConsistencyAnalysisResultView> values) {
        return new ConsistencyAnalysisTableModel(values);
    }
    
    private static class ConsistencyAnalysisTableModel extends SimpleTableModel<UseCaseConsistencyAnalysisResultView> {
        
        private static final long serialVersionUID = -2513156527939282323L;
        
        private static final List<String> COLUMN_NAMES = List.of("Use Case", "# of Traces With Change in Issues", "% of Traces With Change in Issues", "# of Traces With Change in Writes", "% of Traces With Change in Writes");
        
        public ConsistencyAnalysisTableModel(List<UseCaseConsistencyAnalysisResultView> values) {
            super(COLUMN_NAMES, values);
        }

        @Override
        protected Object fieldOf(UseCaseConsistencyAnalysisResultView object, int columnIndex) {
            return switch (columnIndex) {
            case 0 -> object.useCaseName();
            case 1 -> object.numberOfTracesWithChangeInIssues();
            case 2 -> String.format("%.02f", object.percentageWithChangeInIssues());
            case 3 -> object.numberOfTracesWithChangeInWrites();
            case 4 -> String.format("%.02f", object.percentageWithChangeInWrites());
            default -> "";
            };
        }
        
    }
    
}
