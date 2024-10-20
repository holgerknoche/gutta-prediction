package gutta.prediction.ui;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.Entity;
import gutta.prediction.domain.EntityType;
import gutta.prediction.dsl.DeploymentModelReader;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ObservedLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
import gutta.prediction.rewriting.LatencyRewriter;
import gutta.prediction.rewriting.RewrittenEventTrace;
import gutta.prediction.rewriting.TransactionContextRewriter;
import gutta.prediction.span.TraceBuilder;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

public class SpanSimulationFrame extends JFrame {

    private static final long serialVersionUID = 7103275564287849217L;

    private final InitializeOnce<JPanel> mainPanel = new InitializeOnce<>(this::createMainPanel);
    
    private final InitializeOnce<JPanel> originalTracePanel = new InitializeOnce<>(this::createOriginalTracePanel);
    
    private final InitializeOnce<JScrollPane> originalTracePane = new InitializeOnce<>(this::createOriginalTracePane);         
    
    private final InitializeOnce<TraceViewComponent> originalTraceView = new InitializeOnce<>(this::createOriginalTraceView);
    
    private final InitializeOnce<JScrollPane> originalDeploymentModelPane = new InitializeOnce<>(this::createOriginalDeploymentModelPane);
    
    private final InitializeOnce<JTextArea> originalDeploymentModelArea = new InitializeOnce<>(this::createOriginalDeploymentModelArea);
    
    private final InitializeOnce<JPanel> simulatedTracePanel = new InitializeOnce<>(this::createSimulatedTracePanel);
    
    private final InitializeOnce<JScrollPane> simulatedTracePane = new InitializeOnce<>(this::createSimulatedTracePane);         
    
    private final InitializeOnce<TraceViewComponent> simulatedTraceView = new InitializeOnce<>(this::createSimulatedTraceView);
    
    private final InitializeOnce<JScrollPane> simulatedDeploymentModelPane = new InitializeOnce<>(this::createSimulatedDeploymentModelPane);
    
    private final InitializeOnce<JTextArea> simulatedDeploymentModelArea = new InitializeOnce<>(this::createSimulatedDeploymentModelArea);
    
    private final InitializeOnce<JToolBar> simulationToolbar = new InitializeOnce<>(this::createSimulationToolbar);
    
    private final InitializeOnce<JButton> updateViewButton = new InitializeOnce<>(this::createUpdateViewButton);
    
    private final InitializeOnce<JButton> resetButton = new InitializeOnce<>(this::createResetButton);
    
    private EventTrace trace;
    
    private String originalDeploymentModelSpec;
    
    private DeploymentModel originalDeploymentModel;
    
    public SpanSimulationFrame() {
        this.initialize();
        
        this.initializeTestTrace();
        
        this.initializeControls();
    }
    
    private void initialize() {
        this.setTitle("Span Simulation Test");
        this.setSize(1280, 768);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initializeControls() {
        this.setLayout(new BorderLayout());
        
        this.add(this.mainPanel.get(), BorderLayout.CENTER);
        this.add(this.simulationToolbar.get(), BorderLayout.SOUTH);
    }
    
    private void initializeTestTrace() {
        var traceId = 1234;
        var location = new ObservedLocation("test", 1234, 1);
        
        var entityType = new EntityType("et1");
        var entity = new Entity(entityType, "1");
        
        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location, "usecase"),
                new ServiceCandidateInvocationEvent(traceId, 150, location, "sc1"),
                new ServiceCandidateEntryEvent(traceId, 150, location, "sc1"),
                
                new TransactionStartEvent(traceId, 180, location, "tx1"),
                
                new EntityReadEvent(traceId, 200, location, entity),
                
                new ServiceCandidateInvocationEvent(traceId, 220, location, "sc2"),
                new ServiceCandidateEntryEvent(traceId, 220, location, "sc2"),
                
                new EntityWriteEvent(traceId, 300, location, entity),
                
                new ServiceCandidateExitEvent(traceId, 700, location, "sc2"),
                new ServiceCandidateReturnEvent(traceId, 700, location, "sc2"),
                
                new TransactionCommitEvent(traceId, 740, location, "tx1"),
                
                new ServiceCandidateExitEvent(traceId, 760, location, "sc1"),
                new ServiceCandidateReturnEvent(traceId, 760, location, "sc1"),
                new UseCaseEndEvent(traceId, 800, location, "usecase")                
                );
        
        var originalDeploymentModelSpec = 
                "Component \"Component 1\" {\n" +
                "    UseCase usecase\n" +
                "    ServiceCandidate sc1\n" +
                "    ServiceCandidate sc2\n" +
                "}";
        
        this.originalDeploymentModelArea.get().setText(originalDeploymentModelSpec);
        
        var originalDeploymentModel = new DeploymentModelReader().readModel(originalDeploymentModelSpec);        
        var spanTrace = new TraceBuilder().buildTrace(trace, originalDeploymentModel, Set.of());
        this.originalTraceView.get().trace(spanTrace);
        
        this.originalDeploymentModelSpec = originalDeploymentModelSpec;
        this.originalDeploymentModel = originalDeploymentModel;
        this.trace = trace;
    }
    
    private JPanel createMainPanel() {
        var mainPanel = new JPanel();
        
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(this.originalTracePanel.get());
        mainPanel.add(this.simulatedTracePanel.get());
        
        return mainPanel;
    }
        
    private JPanel createOriginalTracePanel() {
        var panel = new JPanel();
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(this.originalTracePane.get());
        panel.add(this.originalDeploymentModelPane.get());
        
        return panel;
    }
    
    private JScrollPane createOriginalTracePane() {
        var scrollPane = new JScrollPane();
        
        scrollPane.setBorder(BorderFactory.createTitledBorder("Original Trace"));
        scrollPane.setViewportView(this.originalTraceView.get());
        
        return scrollPane;
    }
    
    private TraceViewComponent createOriginalTraceView() {
        return new TraceViewComponent();
    }
    
    private JScrollPane createOriginalDeploymentModelPane() {
        var scrollPane = new JScrollPane();
        
        scrollPane.setBorder(BorderFactory.createTitledBorder("Original Deployment Model"));
        scrollPane.setViewportView(this.originalDeploymentModelArea.get());
        
        return scrollPane;
    }
    
    private JTextArea createOriginalDeploymentModelArea() {
        var textArea = new JTextArea();
        
        textArea.setText("Huba");
        textArea.setEditable(false);
        
        return textArea;
    }
    
    private JPanel createSimulatedTracePanel() {
        var panel = new JPanel();
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(this.simulatedTracePane.get());
        panel.add(this.simulatedDeploymentModelPane.get());
        
        return panel;
    }
    
    private JScrollPane createSimulatedTracePane() {
        var scrollPane = new JScrollPane();
        
        scrollPane.setBorder(BorderFactory.createTitledBorder("Simulated Trace"));
        scrollPane.setViewportView(this.simulatedTraceView.get());
        
        return scrollPane;
    }
    
    private TraceViewComponent createSimulatedTraceView() {
        return new TraceViewComponent();
    }
    
    private JScrollPane createSimulatedDeploymentModelPane() {
        var scrollPane = new JScrollPane();
        
        scrollPane.setBorder(BorderFactory.createTitledBorder("Simulated Deployment Model"));
        scrollPane.setViewportView(this.simulatedDeploymentModelArea.get());
        
        return scrollPane;
    }
    
    private JTextArea createSimulatedDeploymentModelArea() {
        var textArea = new JTextArea();
        
        textArea.setText(this.originalDeploymentModelSpec);
        
        return textArea;
    }
    
    private JToolBar createSimulationToolbar() {
        var toolBar = new JToolBar();
        
        toolBar.add(this.updateViewButton.get());
        toolBar.add(this.resetButton.get());
        
        return toolBar;
    }
    
    private JButton createUpdateViewButton() {
        var button = new JButton("Update View");
        
        button.addActionListener(this::updateViewAction);
        
        return button;
    }
    
    private JButton createResetButton() {
        var button = new JButton("Reset");
        
        button.addActionListener(this::resetAction);
        
        return button;
    }
    
    private void updateViewAction(ActionEvent event) {
        var deploymentModelSpec = this.simulatedDeploymentModelArea.get().getText();
        
        var modifieldDeploymentModel = new DeploymentModelReader().readModel(deploymentModelSpec, this.originalDeploymentModel);        
        var rewrittenTrace = this.rewriteTrace(this.trace, modifieldDeploymentModel);
        
        var spanTrace = new TraceBuilder().buildTrace(rewrittenTrace, modifieldDeploymentModel, Set.of());
        this.simulatedTraceView.get().trace(spanTrace);
    }
    
    private RewrittenEventTrace rewriteTrace(EventTrace trace, DeploymentModel modifiedDeploymentModel) {
        var latencyRewriter = new LatencyRewriter(modifiedDeploymentModel);
        var transactionRewriter = new TransactionContextRewriter(modifiedDeploymentModel);
        return transactionRewriter.rewriteTrace(latencyRewriter.rewriteTrace(trace));
    }
    
    private void resetAction(ActionEvent event) {
        this.simulatedDeploymentModelArea.get().setText(this.originalDeploymentModelSpec);
    }

}
