package gutta.prediction.ui;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.dsl.DeploymentModelReader;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.table.TableModel;

abstract class AnalysisFrameTemplate extends UIFrameTemplate {

    private static final long serialVersionUID = -3970730703468529115L;

    private final InitializeOnce<JPanel> mainPanel = new InitializeOnce<>(this::createMainPanel);
    
    private final InitializeOnce<JScrollPane> originalDeploymentModelPane = new InitializeOnce<>(this::createOriginalDeploymentModelPane);
    
    private final InitializeOnce<JTextArea> originalDeploymentModelArea = new InitializeOnce<>(this::createOriginalDeploymentModelArea);
    
    private final InitializeOnce<JScrollPane> modifiedDeploymentModelPane = new InitializeOnce<>(this::createModifiedDeploymentModelPane);
    
    private final InitializeOnce<JTextArea> modifiedDeploymentModelArea = new InitializeOnce<>(this::createModifiedDeploymentModelArea);
    
    private final InitializeOnce<JScrollPane> resultsTablePane = new InitializeOnce<>(this::createResultsTablePane);
    
    private final InitializeOnce<JTable> resultsTable = new InitializeOnce<>(this::createResultsTable);

    private final InitializeOnce<JToolBar> toolBar = new InitializeOnce<>(this::createToolBar);
    
    private final InitializeOnce<JButton> analyzeButton = new InitializeOnce<>(this::createAnalyzeButton);
        
    private final InitializeOnce<JButton> resetButton = new InitializeOnce<>(this::createResetButton);

    private final String originalDeploymentModelSpec;
    
    private final DeploymentModel originalDeploymentModel;
    
    private final String givenModifiedDeploymentModelSpec;
    
    protected AnalysisFrameTemplate(String originalDeploymentModelSpec, DeploymentModel originalDeploymentModel) {
        this(originalDeploymentModelSpec, originalDeploymentModel, null);
    }
    
    protected AnalysisFrameTemplate(String originalDeploymentModelSpec, DeploymentModel originalDeploymentModel, String givenModifiedDeploymentModelSpec) {
        this.originalDeploymentModelSpec = originalDeploymentModelSpec;
        this.originalDeploymentModel = originalDeploymentModel;
        this.givenModifiedDeploymentModelSpec = givenModifiedDeploymentModelSpec;
    }
    
    protected void initializeControls() {
        this.setLayout(new BorderLayout());
        
        this.add(this.mainPanel.get(), BorderLayout.CENTER);
        this.add(this.toolBar.get(), BorderLayout.SOUTH);
    }

    protected void initializeDefaults() {
        this.originalDeploymentModelArea.get().setText(this.originalDeploymentModelSpec);
        
        if (this.givenModifiedDeploymentModelSpec != null) {
            this.modifiedDeploymentModelArea.get().setText(this.givenModifiedDeploymentModelSpec);
            this.analyzeScenario(this.givenModifiedDeploymentModelSpec);
        }
    }
    
    private JScrollPane createOriginalDeploymentModelPane() {
        var pane = new JScrollPane(this.originalDeploymentModelArea.get());
        
        pane.setBorder(BorderFactory.createTitledBorder("Original Deployment Model"));
        
        return pane;
    }
    
    private JPanel createMainPanel() {
        var panel = new JPanel();        
        var layout = new SimpleGridBagLayout(panel, 2, 2);
        
        layout.add(this.originalDeploymentModelPane.get(), 0, 0, 1, 1);
        layout.add(this.modifiedDeploymentModelPane.get(), 1, 0, 1, 1);
        layout.add(this.resultsTablePane.get(), 0, 1, 2, 1);
        
        return panel;
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
    
    private JScrollPane createResultsTablePane() {
        return new JScrollPane(this.resultsTable.get());
    }
    
    private JTable createResultsTable() {
        var table = new JTable();
        
        table.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    var table = AnalysisFrameTemplate.this.resultsTable.get();
                    
                    var rowIndex = table.rowAtPoint(event.getPoint());
                    AnalysisFrameTemplate.this.onRowSelection(table, rowIndex);
                }                
            }
            
        });
        
        return table;
    }

    protected void onRowSelection(JTable table, int rowIndex) {
        // Do nothing by default
    }
    
    protected void setResultsTableModel(TableModel tableModel) {
        this.resultsTable.get().setModel(tableModel);
    }
    
    protected String originalDeploymentModelSpec() {
        return this.originalDeploymentModelSpec;
    }
    
    protected DeploymentModel originalDeploymentModel() {
        return this.originalDeploymentModel;
    }
    
    protected String givenModifiedDeploymentModelSpec() {
        return this.givenModifiedDeploymentModelSpec;
    }
    
    protected String modifiedDeploymentModelSpec() {
        return this.modifiedDeploymentModelArea.get().getText();
    }
    
    private JToolBar createToolBar() {
        var toolBar = new JToolBar();
        
        toolBar.add(this.analyzeButton.get());
        toolBar.add(this.resetButton.get());
        
        this.addSpecificItemsToToolBar(toolBar);
        
        return toolBar;
    }
    
    protected void addSpecificItemsToToolBar(JToolBar toolBar) {
        // No items by default
    }
    
    private JButton createAnalyzeButton() {
        var button = new JButton("Analyze Scenario");
        
        button.addActionListener(this::analyzeScenarioAction);
        
        return button;
    }
        
    private JButton createResetButton() {
        var button = new JButton("Reset Scenario");
        
        button.addActionListener(this::resetScenarioAction);
        
        return button;
    }
    
    private void analyzeScenarioAction(ActionEvent event) {
        var modifiedDeploymentModelSpec = this.modifiedDeploymentModelArea.get().getText();
        this.analyzeScenario(modifiedDeploymentModelSpec);
    }
    
    private void analyzeScenario(String modifiedDeploymentModelSpec) {
        var modifiedDeploymentModel = new DeploymentModelReader().readModel(modifiedDeploymentModelSpec, this.originalDeploymentModel);        
        this.performScenarioAnalysis(this.originalDeploymentModel, modifiedDeploymentModel);
    }
    
    protected abstract void performScenarioAnalysis(DeploymentModel originalDeploymentModel, DeploymentModel modifiedDeploymentModel);
    
    private void resetScenarioAction(ActionEvent event) {
        var valueToSet = (this.givenModifiedDeploymentModelSpec != null) ? this.givenModifiedDeploymentModelSpec : "";        
        this.modifiedDeploymentModelArea.get().setText(valueToSet);
    }
            
}
