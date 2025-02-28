package gutta.prediction.ui;

import gutta.prediction.analysis.overhead.DurationChangeAnalysis;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.TableModel;

/**
 * Frame to show the overhead analysis results aggregated per use case.
 */
class UseCaseOverheadAnalysisFrame extends UseCaseAnalysisFrameTemplate<UseCaseOverheadAnalysisResultView> {

    private static final double DEFAULT_SIGNIFICANCE_LEVEL = 0.05;

    private static final long serialVersionUID = -5771105484367717055L;

    private final InitializeOnce<JLabel> significanceLevelLabel = new InitializeOnce<>(this::createSignificanceLevelLabel);

    private final InitializeOnce<JTextField> significanceLevelField = new InitializeOnce<>(this::createSignificanceLevelTextField);

    public UseCaseOverheadAnalysisFrame(Map<String, Collection<EventTrace>> tracesPerUseCase, String originalDeploymentModelSpec,
            DeploymentModel originalDeploymentModel) {
        
        super(tracesPerUseCase, originalDeploymentModelSpec, originalDeploymentModel);

        this.initialize();
        this.initializeControls();
        this.initializeDefaults();
    }

    private void initialize() {
        super.initialize("Overhead Change Analysis");
    }

    protected void initializeDefaults() {
        super.initializeDefaults();
        this.significanceLevelField.get().setText(String.valueOf(DEFAULT_SIGNIFICANCE_LEVEL));
    }

    private JLabel createSignificanceLevelLabel() {
        return new JLabel("Significance level:");
    }

    private JTextField createSignificanceLevelTextField() {
        return new JTextField();
    }

    @Override
    protected void addSpecificItemsToToolBar(JToolBar toolBar) {
        toolBar.add(this.significanceLevelLabel.get());
        toolBar.add(this.significanceLevelField.get());
    }

    @Override
    protected void onRowSelection(JTable table, int rowIndex) {
        var useCaseName = (String) table.getValueAt(rowIndex, 0);
        var traces = this.tracesPerUseCase.get(useCaseName);

        if (traces != null) {
            var frame = new TracesForUseCaseOverheadAnalysisFrame(useCaseName, traces, this.originalDeploymentModelSpec(), this.originalDeploymentModel(),
                    this.modifiedDeploymentModelSpec());
            frame.setVisible(true);
        }
    }

    @Override
    protected UseCaseOverheadAnalysisResultView analyzeScenario(String useCaseName, Collection<EventTrace> traces, DeploymentModel originalDeploymentModel,
            DeploymentModel modifiedDeploymentModel) {

        var significanceLevelText = this.significanceLevelField.get().getText();
        var significanceLevel = (significanceLevelText.isEmpty()) ? DEFAULT_SIGNIFICANCE_LEVEL : Double.parseDouble(significanceLevelText);

        var analysisResult = new DurationChangeAnalysis().analyzeTraces(traces, originalDeploymentModel, modifiedDeploymentModel, significanceLevel);
        return new UseCaseOverheadAnalysisResultView(useCaseName, analysisResult);
    }

    @Override
    protected TableModel createTableModel(List<UseCaseOverheadAnalysisResultView> values) {
        return new OverheadAnalysisTableModel(values);
    }

    /**
     * Table model for the results table.
     */
    private static class OverheadAnalysisTableModel extends SimpleTableModel<UseCaseOverheadAnalysisResultView> {

        private static final long serialVersionUID = -8857807589164164128L;

        private static final List<String> COLUMN_NAMES = List.of("Use Case", "Old Avg. Duration", "New Avg. Duration", "Significant Change?", "p Value",
                "Old Avg. # Remote Calls", "New Avg. # Remote Calls");

        public OverheadAnalysisTableModel(List<UseCaseOverheadAnalysisResultView> values) {
            super(COLUMN_NAMES, values);
        }

        @Override
        protected Object fieldOf(UseCaseOverheadAnalysisResultView object, int columnIndex) {
            return switch (columnIndex) {
            case 0 -> object.useCaseName();
            case 1 -> formatAverage(object.originalDuration());
            case 2 -> formatAverage(object.newDuration());
            case 3 -> asYesNo(object.significant());
            case 4 -> formatPValue(object.pValue());
            case 5 -> formatAverage(object.oldAverageNumberOfRemoteCals());
            case 6 -> formatAverage(object.newAverageNumberOfRemoteCalls());
            default -> "";
            };
        }

    }

}
