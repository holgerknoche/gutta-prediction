package gutta.prediction.ui;

import gutta.prediction.analysis.latency.DurationChangeAnalysis;
import gutta.prediction.analysis.latency.DurationChangeAnalysis.Result;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.dsl.DeploymentModelReader;
import gutta.prediction.event.EventTrace;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;

class UseCaseLatencyAnalysisFrame extends UIFrameTemplate {

    private static final double DEFAULT_SIGNIFICANCE_LEVEL = 0.05;
    
    private static final long serialVersionUID = -5771105484367717055L;        

    private final InitializeOnce<JPanel> mainPanel = new InitializeOnce<>(this::createMainPanel);
    
    private final InitializeOnce<JScrollPane> originalDeploymentModelPane = new InitializeOnce<>(this::createOriginalDeploymentModelPane);
    
    private final InitializeOnce<JTextArea> originalDeploymentModelArea = new InitializeOnce<>(this::createOriginalDeploymentModelArea);
    
    private final InitializeOnce<JScrollPane> modifiedDeploymentModelPane = new InitializeOnce<>(this::createModifiedDeploymentModelPane);
    
    private final InitializeOnce<JTextArea> modifiedDeploymentModelArea = new InitializeOnce<>(this::createModifiedDeploymentModelArea);
    
    private final InitializeOnce<JScrollPane> useCasesTablePane = new InitializeOnce<>(this::createUseCasesTablePane);
    
    private final InitializeOnce<JTable> useCasesTable = new InitializeOnce<>(this::createUseCasesTable);
    
    private final InitializeOnce<JToolBar> toolBar = new InitializeOnce<>(this::createToolBar);
    
    private final InitializeOnce<JButton> analyzeButton = new InitializeOnce<>(this::createAnalyzeButton);
    
    private final InitializeOnce<JTextField> significanceLevelField = new InitializeOnce<>(this::createSignificanceLevelTextField);
    
    private final InitializeOnce<JButton> resetButton = new InitializeOnce<>(this::createResetButton);
   
    private final Map<String, Collection<EventTrace>> tracesPerUseCase;
    
    private final String originalDeploymentModelSpec;
    
    private final DeploymentModel originalDeploymentModel;
    
    public UseCaseLatencyAnalysisFrame(Map<String, Collection<EventTrace>> tracesPerUseCase, String originalDeploymentModelSpec, DeploymentModel originalDeploymentModel) {
        this.tracesPerUseCase = tracesPerUseCase;
        this.originalDeploymentModelSpec = originalDeploymentModelSpec;
        this.originalDeploymentModel = originalDeploymentModel;
        
        this.initialize();
        this.initializeControls();
        this.initializeDefaults();
    }
    
    private void initialize() {
        super.initialize("Latency Change Analysis");
    }
    
    private void initializeControls() {
        this.setLayout(new BorderLayout());
        
        this.add(this.mainPanel.get(), BorderLayout.CENTER);
        this.add(this.toolBar.get(), BorderLayout.SOUTH);
    }
    
    private void initializeDefaults() {
        this.originalDeploymentModelArea.get().setText(this.originalDeploymentModelSpec);
    }
    
    private JPanel createMainPanel() {
        var panel = new JPanel();        
        var layout = new SimpleGridBagLayout(panel, 2, 2);
        
        layout.add(this.originalDeploymentModelPane.get(), 0, 0, 1, 1);
        layout.add(this.modifiedDeploymentModelPane.get(), 1, 0, 1, 1);
        layout.add(this.useCasesTablePane.get(), 0, 1, 2, 1);
        
        return panel;
    }
    
    private JScrollPane createOriginalDeploymentModelPane() {
        var pane = new JScrollPane(this.originalDeploymentModelArea.get());
        
        pane.setBorder(BorderFactory.createTitledBorder("Original Deployment Model"));
        
        return pane;
    }
    
    private JTextArea createOriginalDeploymentModelArea() {
        var textArea = new JTextArea();
        
        textArea.setFont(MONOSPACED_FONT);
        textArea.setEditable(false);
        
        return textArea;
    }
    
    private JScrollPane createModifiedDeploymentModelPane() {
        var pane = new JScrollPane(this.modifiedDeploymentModelArea.get());
        
        pane.setBorder(BorderFactory.createTitledBorder("Modified Deployment Model"));
        
        return pane;
    }
    
    private JTextArea createModifiedDeploymentModelArea() {
        var textArea = new JTextArea();
        
        textArea.setFont(MONOSPACED_FONT);
        
        return textArea;
    }
    
    private JScrollPane createUseCasesTablePane() {
        return new JScrollPane(this.useCasesTable.get());
    }
   
    private JTable createUseCasesTable() {
        return new JTable();
    }
    
    private JToolBar createToolBar() {
        var toolBar = new JToolBar();
        
        toolBar.add(this.analyzeButton.get());
        toolBar.add(this.significanceLevelField.get());
        toolBar.add(this.resetButton.get());
        
        return toolBar;
    }
    
    private JButton createAnalyzeButton() {
        var button = new JButton("Analyze Scenario");
        
        button.addActionListener(this::analyzeScenarioAction);
        
        return button;
    }
    
    private JTextField createSignificanceLevelTextField() {
        return new JTextField();
    }
    
    private JButton createResetButton() {
        var button = new JButton("Reset Scenario");
        
        button.addActionListener(this::resetScenarioAction);
        
        return button;
    }
    
    private void analyzeScenarioAction(ActionEvent event) {
        var modifiedDeploymentModelSpec = this.modifiedDeploymentModelArea.get().getText();
        var modifiedDeploymentModel = new DeploymentModelReader().readModel(modifiedDeploymentModelSpec, this.originalDeploymentModel);
        
        var significanceLevelText = this.significanceLevelField.get().getText();
        var significanceLevel = (significanceLevelText.isEmpty()) ? DEFAULT_SIGNIFICANCE_LEVEL : Double.parseDouble(significanceLevelText);        
                     
        var resultViews = new ArrayList<ResultView>();
        
        for (var entry : this.tracesPerUseCase.entrySet()) {
            var useCaseName = entry.getKey();
            var traces = entry.getValue();
            
            var analysisResult = new DurationChangeAnalysis().analyzeTraces(traces, this.originalDeploymentModel, modifiedDeploymentModel, significanceLevel);
            resultViews.add(new ResultView(useCaseName, analysisResult));
        }
        
        resultViews.sort((view1, view2) -> view1.useCaseName().compareTo(view2.useCaseName()));
        
        this.refreshUseCasesTable(resultViews);
    }
    
    private void resetScenarioAction(ActionEvent event) {
        this.modifiedDeploymentModelArea.get().setText("");
    }
    
    private void refreshUseCasesTable(List<ResultView> values) {
        var tableModel = new LatencyAnalysisTableModel(values);
        this.useCasesTable.get().setModel(tableModel);
    }
    
    private record ResultView(String useCaseName, double originalDuration, double newDuration, boolean significant, double pValue) {
        
        public ResultView(String useCaseName, Result result) {
            this(useCaseName, result.originalMean(), result.modifiedMean(), result.significantChange(), result.pValue());
        }
        
    }
    
    private static class LatencyAnalysisTableModel extends SimpleTableModel<ResultView> {
        
        private static final long serialVersionUID = -8857807589164164128L;
        
        private static final List<String> COLUMN_NAMES = List.of("Use Case", "Old Avg. Duration", "New Avg. Duration", "Significant Change?", "p Value");
        
        public LatencyAnalysisTableModel(List<ResultView> values) {
            super(COLUMN_NAMES, values);
        }

        @Override
        protected Object fieldOf(ResultView object, int columnIndex) {
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
