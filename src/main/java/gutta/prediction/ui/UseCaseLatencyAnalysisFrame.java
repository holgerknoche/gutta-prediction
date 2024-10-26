package gutta.prediction.ui;

import gutta.prediction.analysis.latency.DurationChangeAnalysis;
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

class UseCaseLatencyAnalysisFrame extends UseCaseAnalysisFrameTemplate<UseCaseLatencyAnalysisResultView> {

    private static final double DEFAULT_SIGNIFICANCE_LEVEL = 0.05;
    
    private static final long serialVersionUID = -5771105484367717055L;        
    
    private final InitializeOnce<JLabel> significanceLevelLabel = new InitializeOnce<>(this::createSignificanceLevelLabel);
    
    private final InitializeOnce<JTextField> significanceLevelField = new InitializeOnce<>(this::createSignificanceLevelTextField);
        
    public UseCaseLatencyAnalysisFrame(Map<String, Collection<EventTrace>> tracesPerUseCase, String originalDeploymentModelSpec, DeploymentModel originalDeploymentModel) {
        super(tracesPerUseCase, originalDeploymentModelSpec, originalDeploymentModel);
        
        this.initialize();
        this.initializeControls();
        this.initializeDefaults();
    }
    
    private void initialize() {
        super.initialize("Latency Change Analysis");
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
            var frame = new TracesPerUseCaseLatencyAnalysisFrame(useCaseName, traces, this.originalDeploymentModelSpec(), this.originalDeploymentModel());
            frame.setVisible(true);
        }
    }
            
    @Override
    protected UseCaseLatencyAnalysisResultView analyzeScenario(String useCaseName, Collection<EventTrace> traces, DeploymentModel originalDeploymentModel,
            DeploymentModel modifiedDeploymentModel) {

        var significanceLevelText = this.significanceLevelField.get().getText();
        var significanceLevel = (significanceLevelText.isEmpty()) ? DEFAULT_SIGNIFICANCE_LEVEL : Double.parseDouble(significanceLevelText);
        
        var analysisResult = new DurationChangeAnalysis().analyzeTraces(traces, originalDeploymentModel, modifiedDeploymentModel, significanceLevel);
        return new UseCaseLatencyAnalysisResultView(useCaseName, analysisResult);
    }
    
    @Override
    protected TableModel createTableModel(List<UseCaseLatencyAnalysisResultView> values) {
        return new LatencyAnalysisTableModel(values);
    }
                
    private static class LatencyAnalysisTableModel extends SimpleTableModel<UseCaseLatencyAnalysisResultView> {
        
        private static final long serialVersionUID = -8857807589164164128L;
        
        private static final List<String> COLUMN_NAMES = List.of("Use Case", "Old Avg. Duration", "New Avg. Duration", "Significant Change?", "p Value");
        
        public LatencyAnalysisTableModel(List<UseCaseLatencyAnalysisResultView> values) {
            super(COLUMN_NAMES, values);
        }

        @Override
        protected Object fieldOf(UseCaseLatencyAnalysisResultView object, int columnIndex) {
            return switch (columnIndex) {
            case 0 -> object.useCaseName();
            case 1 -> String.format("%.02f", object.originalDuration());
            case 2 -> String.format("%.02f", object.newDuration());
            case 3 -> object.significant();
            case 4 -> String.format("%.04f", object.pValue());
            default -> "";
            };
        }
        
    }
    
}
