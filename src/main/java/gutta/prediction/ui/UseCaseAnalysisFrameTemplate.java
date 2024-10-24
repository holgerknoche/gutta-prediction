package gutta.prediction.ui;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.dsl.DeploymentModelReader;
import gutta.prediction.event.EventTrace;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.table.TableModel;

abstract class UseCaseAnalysisFrameTemplate<T extends Comparable<T>> extends UIFrameTemplate {
    
    private static final long serialVersionUID = 5209108354998887792L;

    private final InitializeOnce<JPanel> mainPanel = new InitializeOnce<>(this::createMainPanel);
    
    private final InitializeOnce<JScrollPane> originalDeploymentModelPane = new InitializeOnce<>(this::createOriginalDeploymentModelPane);
    
    private final InitializeOnce<JTextArea> originalDeploymentModelArea = new InitializeOnce<>(this::createOriginalDeploymentModelArea);
    
    private final InitializeOnce<JScrollPane> modifiedDeploymentModelPane = new InitializeOnce<>(this::createModifiedDeploymentModelPane);
    
    private final InitializeOnce<JTextArea> modifiedDeploymentModelArea = new InitializeOnce<>(this::createModifiedDeploymentModelArea);
    
    private final InitializeOnce<JScrollPane> useCasesTablePane = new InitializeOnce<>(this::createUseCasesTablePane);
    
    private final InitializeOnce<JTable> useCasesTable = new InitializeOnce<>(this::createUseCasesTable);
    
    private final InitializeOnce<JToolBar> toolBar = new InitializeOnce<>(this::createToolBar);
    
    private final InitializeOnce<JButton> analyzeButton = new InitializeOnce<>(this::createAnalyzeButton);
        
    private final InitializeOnce<JButton> resetButton = new InitializeOnce<>(this::createResetButton);
   
    private final Map<String, Collection<EventTrace>> tracesPerUseCase;
    
    private final String originalDeploymentModelSpec;
    
    private final DeploymentModel originalDeploymentModel;

    protected UseCaseAnalysisFrameTemplate(Map<String, Collection<EventTrace>> tracesPerUseCase, String originalDeploymentModelSpec, DeploymentModel originalDeploymentModel) {
        this.tracesPerUseCase = tracesPerUseCase;
        this.originalDeploymentModelSpec = originalDeploymentModelSpec;
        this.originalDeploymentModel = originalDeploymentModel;
    }

    protected void initializeControls() {
        this.setLayout(new BorderLayout());
        
        this.add(this.mainPanel.get(), BorderLayout.CENTER);
        this.add(this.toolBar.get(), BorderLayout.SOUTH);
    }
    
    protected void initializeDefaults() {
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
        toolBar.add(this.resetButton.get());
        
        this.addSpecificItemsToToolBar(toolBar);
        
        return toolBar;
    }
    
    protected abstract void addSpecificItemsToToolBar(JToolBar toolBar);
    
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
        var modifiedDeploymentModel = new DeploymentModelReader().readModel(modifiedDeploymentModelSpec, this.originalDeploymentModel);
                             
        var results = new ArrayList<T>();        
        for (var entry : this.tracesPerUseCase.entrySet()) {
            var useCaseName = entry.getKey();
            var traces = entry.getValue();
            
            var result = this.analyzeScenario(useCaseName, traces, this.originalDeploymentModel, modifiedDeploymentModel);
            results.add(result);
        }                
        
        Collections.sort(results);
        
        this.refreshUseCasesTable(results);
    }
    
    protected abstract T analyzeScenario(String useCaseName, Collection<EventTrace> traces, DeploymentModel originalDeploymentModel, DeploymentModel modifiedDeploymentModel);        
    
    private void resetScenarioAction(ActionEvent event) {
        this.modifiedDeploymentModelArea.get().setText("");
    }
    
    private void refreshUseCasesTable(List<T> values) {
        var tableModel = this.createTableModel(values);
        this.useCasesTable.get().setModel(tableModel);
    }
    
    protected abstract TableModel createTableModel(List<T> values);
    
}
