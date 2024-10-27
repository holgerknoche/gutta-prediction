package gutta.prediction.ui;

import gutta.prediction.analysis.latency.DurationChangeAnalysis;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;

import java.util.Collection;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.TableModel;

class TracesForUseCaseLatencyAnalysisFrame extends TracesForUseCaseAnalysisFrameTemplate<TraceLatencyAnalysisResultView> {

    private static final long serialVersionUID = -8761502500290812925L;

    public TracesForUseCaseLatencyAnalysisFrame(String useCaseName, Collection<EventTrace> traces, String originalDeploymentModelSpec, DeploymentModel originalDeploymentModel, String givenModifiedDeploymentModelSpec) {
        super(traces, originalDeploymentModelSpec, originalDeploymentModel, givenModifiedDeploymentModelSpec);
        
        this.initialize("Latency Analysis for Use Case '" + useCaseName + "'");
        this.initializeControls();
        this.initializeDefaults();
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
    protected TraceLatencyAnalysisResultView analyzeScenario(EventTrace trace, DeploymentModel originalDeploymentModel, DeploymentModel modifiedDeploymentModel) {
        var analysisResult = new DurationChangeAnalysis().analyzeTraces(List.of(trace), originalDeploymentModel, modifiedDeploymentModel, 0.05);
        
        var originalDuration = analysisResult.originalMean();
        var modifiedDuration = analysisResult.modifiedMean();
        
        var changePercentage = (modifiedDuration == 0.0) ? 0.0 : (modifiedDuration / originalDuration) - 1.0;
        
        return new TraceLatencyAnalysisResultView(trace.traceId(), analysisResult.originalMean(), analysisResult.modifiedMean(), changePercentage);
    }

    @Override
    protected TableModel createTableModel(List<TraceLatencyAnalysisResultView> values) {
        return new TraceLatencyTableModel(values);
    }
    
    private static class TraceLatencyTableModel extends SimpleTableModel<TraceLatencyAnalysisResultView> {

        private static final long serialVersionUID = -3816177678731840041L;
        
        private static final List<String> COLUMN_NAMES = List.of("Trace ID", "Original Duration", "Modified Duration", "Change %");

        public TraceLatencyTableModel(List<TraceLatencyAnalysisResultView> values) {
            super(COLUMN_NAMES, values);
        }
        
        @Override
        protected Object fieldOf(TraceLatencyAnalysisResultView object, int columnIndex) {
            return switch (columnIndex) {
            case 0 -> object.traceId();
            case 1 -> String.format("%.02f", object.originalDuration());
            case 2 -> String.format("%.02f", object.newDuration());
            case 3 -> String.format("%.02f", object.changePercentage());
            default -> "";
            };
        }
        
    }
    
}
