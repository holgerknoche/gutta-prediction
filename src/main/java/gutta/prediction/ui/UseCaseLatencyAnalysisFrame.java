package gutta.prediction.ui;

import gutta.prediction.domain.DeploymentModel;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

class UseCaseLatencyAnalysisFrame extends UIFrameTemplate {

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
    
    private final InitializeOnce<JButton> resetButton = new InitializeOnce<>(this::createResetButton);
    
    private final String originalDeploymentModelSpec;
    
    private final DeploymentModel originalDeploymentModel;
    
    public UseCaseLatencyAnalysisFrame(String originalDeploymentModelSpec, DeploymentModel originalDeploymentModel) {
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
        toolBar.add(this.resetButton.get());
        
        return toolBar;
    }
    
    private JButton createAnalyzeButton() {
        var button = new JButton("Analyze Scenario");
        
        return button;
    }
    
    private JButton createResetButton() {
        var button = new JButton("Reset Scenario");
        
        return button;
    }
    
}
