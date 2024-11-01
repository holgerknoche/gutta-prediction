package gutta.prediction.ui;

import gutta.prediction.analysis.consistency.CheckCrossComponentAccesses;
import gutta.prediction.analysis.consistency.ConsistencyIssuesAnalysis;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;

import java.util.Collection;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.TableModel;

class TracesForUseCaseConsistencyAnalysisFrame extends TracesForUseCaseAnalysisFrameTemplate<TraceConsistencyAnalysisResultView> {

    private static final long serialVersionUID = 1597680404131271009L;

    public TracesForUseCaseConsistencyAnalysisFrame(String useCaseName, Collection<EventTrace> traces, String originalDeploymentModelSpec, DeploymentModel originalDeploymentModel, String givenModifiedDeploymentModelSpec) {
        super(traces, originalDeploymentModelSpec, originalDeploymentModel, givenModifiedDeploymentModelSpec);
        
        this.initialize("Consistency Analysis for Use Case '" + useCaseName + "'");
        this.initializeControls();
        this.initializeDefaults();
    }

    @Override
    protected TraceConsistencyAnalysisResultView analyzeScenario(EventTrace trace, DeploymentModel originalDeploymentModel, DeploymentModel modifiedDeploymentModel) {
        var result = new ConsistencyIssuesAnalysis(CheckCrossComponentAccesses.YES).analyzeTrace(trace, originalDeploymentModel, modifiedDeploymentModel);
        
        var numberOfChangedIssues = result.newIssues().size() + result.obsoleteIssues().size();
        var issuesChanged = (numberOfChangedIssues > 0);
        
        var numberOfChangedWrites = result.nowCommittedWrites().size() + result.nowRevertedWrites().size();
        var numberOfUnchangedWrites = result.unchangedCommittedWrites().size() + result.unchangedRevertedWrites().size();
        var writesChanged = (numberOfChangedWrites > 0);
        
        return new TraceConsistencyAnalysisResultView(trace.traceId(), issuesChanged, numberOfChangedIssues, result.unchangedIssues().size(), writesChanged, numberOfChangedWrites, numberOfUnchangedWrites);
    }
    
    @Override
    protected void onRowSelection(JTable table, int rowIndex) {
        var traceId = (Long) table.getValueAt(rowIndex, 0);
        var trace = this.traceWithId(traceId);              
        
        if (trace != null) {
            var frame = new TraceAnalysisFrame(trace, this.originalDeploymentModelSpec(), this.originalDeploymentModel(), this.givenModifiedDeploymentModelSpec());
            frame.setVisible(true);
        }
    }

    @Override
    protected TableModel createTableModel(List<TraceConsistencyAnalysisResultView> values) {
        return new TraceConsistencyAnalysisTableModel(values);
    }
    
    private static class TraceConsistencyAnalysisTableModel extends SimpleTableModel<TraceConsistencyAnalysisResultView> {
        
        private static final long serialVersionUID = -8684724539167864827L;
        
        private static final List<String> COLUMN_NAMES = List.of("Trace ID", "Issues Changed?", "# of Changed Issues", "# of Unchanged Issues", "Writes Changed?", "# of Changed Writes", "# of Unchanged Writes");

        public TraceConsistencyAnalysisTableModel(List<TraceConsistencyAnalysisResultView> values) {
            super(COLUMN_NAMES, values);
        }

        @Override
        protected Object fieldOf(TraceConsistencyAnalysisResultView object, int columnIndex) {
            return switch (columnIndex) {
            case 0 -> object.traceId();
            case 1 -> asYesNo(object.issuesChanged());
            case 2 -> object.numberOfChangedIssues();
            case 3 -> object.numberOfUnchangedIssues();
            case 4 -> asYesNo(object.writesChanged());
            case 5 -> object.numberOfChangedWrites();
            case 6 -> object.numberOfUnchangedWrites();
            default -> "";
            };
        }
        
    }
    
}
