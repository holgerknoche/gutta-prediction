package gutta.prediction.ui;

import gutta.prediction.analysis.overhead.DurationChangeAnalysis;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;

import java.util.Collection;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * Frame to show the invocation overhead analysis results for the traces of a use case.
 */
class TracesForUseCaseOverheadAnalysisFrame extends TracesForUseCaseAnalysisFrameTemplate<TraceOverheadAnalysisResultView> {

    private static final long serialVersionUID = -8761502500290812925L;

    public TracesForUseCaseOverheadAnalysisFrame(String useCaseName, Collection<EventTrace> traces, String originalDeploymentModelSpec,
            DeploymentModel originalDeploymentModel, String givenModifiedDeploymentModelSpec) {
        super(traces, originalDeploymentModelSpec, originalDeploymentModel, givenModifiedDeploymentModelSpec);

        this.initialize("Overhead Analysis for Use Case '" + useCaseName + "'");
        this.initializeControls();
        this.initializeDefaults();
    }

    @Override
    protected void onRowSelection(JTable table, int rowIndex) {
        var traceId = (Long) table.getValueAt(rowIndex, 0);
        var trace = this.traceWithId(traceId);

        if (trace != null) {
            var frame = new TraceAnalysisFrame(trace, this.originalDeploymentModelSpec(), this.originalDeploymentModel(),
                    this.givenModifiedDeploymentModelSpec());
            frame.setVisible(true);
        }
    }

    @Override
    protected TraceOverheadAnalysisResultView analyzeScenario(EventTrace trace, DeploymentModel originalDeploymentModel,
            DeploymentModel modifiedDeploymentModel) {
        var analysisResult = new DurationChangeAnalysis().analyzeTraces(List.of(trace), originalDeploymentModel, modifiedDeploymentModel, 0.05);
        return new TraceOverheadAnalysisResultView(trace.traceId(), analysisResult.originalMean(), analysisResult.modifiedMean(),
                analysisResult.oldAverageNumberOfRemoteCalls(), analysisResult.newAverageNumberOfRemoteCalls());
    }

    @Override
    protected TableModel createTableModel(List<TraceOverheadAnalysisResultView> values) {
        return new TraceOverheadTableModel(values);
    }

    /**
     * Table model for the results table.
     */
    private static class TraceOverheadTableModel extends SimpleTableModel<TraceOverheadAnalysisResultView> {

        private static final long serialVersionUID = -3816177678731840041L;

        private static final List<String> COLUMN_NAMES = List.of("Trace ID", "Original Duration", "Modified Duration", "Duration Change %",
                "Original # Remote Calls", "Modified # Remote Calls", "Remote Calls Change %");

        public TraceOverheadTableModel(List<TraceOverheadAnalysisResultView> values) {
            super(COLUMN_NAMES, values);
        }

        @Override
        protected Object fieldOf(TraceOverheadAnalysisResultView object, int columnIndex) {
            return switch (columnIndex) {
            case 0 -> object.traceId();
            case 1 -> formatAverage(object.originalDuration());
            case 2 -> formatAverage(object.newDuration());
            case 3 -> formatPercentage(object.durationChangePercentage());
            case 4 -> formatAverage(object.oldNumberOfRemoteCalls());
            case 5 -> formatAverage(object.newNumberOfRemoteCalls());
            case 6 -> formatPercentage(object.remoteCallsChangePercentage());
            default -> "";
            };
        }

    }

}
