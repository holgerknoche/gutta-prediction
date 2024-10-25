package gutta.prediction.ui;

import gutta.prediction.analysis.consistency.ConsistencyIssue;
import gutta.prediction.analysis.consistency.ConsistencyIssuesAnalysis;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.dsl.DeploymentModelReader;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.span.TraceBuilder;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

class TraceAnalysisFrame extends UIFrameTemplate {

    private static final long serialVersionUID = -5946710894820989364L;
    
    private final InitializeOnce<JPanel> mainPanel = new InitializeOnce<>(this::createMainPanel);
    
    private final InitializeOnce<JScrollPane> originalModelPane = new InitializeOnce<>(this::createOriginalModelPane);
    
    private final InitializeOnce<JTextArea> originalModelArea = new InitializeOnce<>(this::createOriginalModelArea);
        
    private final InitializeOnce<JScrollPane> modifiedModelPane = new InitializeOnce<>(this::createModifiedModelPane);
    
    private final InitializeOnce<JTextArea> modifiedModelArea = new InitializeOnce<>(this::createModifiedModelArea);
    
    private final InitializeOnce<JTabbedPane> resultPane = new InitializeOnce<>(this::createResultPane);
    
    private final InitializeOnce<JScrollPane> traceViewPane = new InitializeOnce<>(this::createTraceViewPane);
    
    private final InitializeOnce<TraceViewComponent> traceView = new InitializeOnce<>(this::createTraceView);
    
    private final InitializeOnce<JScrollPane> issuesTablePane = new InitializeOnce<>(this::createIssuesTablePane); 
    
    private final InitializeOnce<JTable> issuesTable = new InitializeOnce<>(this::createIssuesTable);
    
    private final InitializeOnce<JScrollPane> writesTablePane = new InitializeOnce<>(this::createWritesTablePane); 
    
    private final InitializeOnce<JTable> writesTable = new InitializeOnce<>(this::createWritesTable);

    private final InitializeOnce<JToolBar> toolBar = new InitializeOnce<>(this::createToolBar);
    
    private final InitializeOnce<JButton> analyzeScenarioButton = new InitializeOnce<>(this::createAnalyzeScenarioButton);
    
    private final InitializeOnce<JButton> resetScenarioButton = new InitializeOnce<>(this::createResetScenarioButton);

    private final EventTrace trace;
    
    private final String originalDeploymentModelSpec;
    
    private final DeploymentModel originalDeploymentModel;
    
    private final String givenModifiedDeploymentModelSpec;
        
    public TraceAnalysisFrame(EventTrace trace, String originalDeploymentModelSpec, DeploymentModel originalDeploymentModel) {
        this.trace = trace;
        this.originalDeploymentModelSpec = originalDeploymentModelSpec;
        this.originalDeploymentModel = originalDeploymentModel;
        this.givenModifiedDeploymentModelSpec = null;
        
        this.initialize();
        this.initializeControls();
        this.initializeDefaults();
        
        if (this.givenModifiedDeploymentModelSpec != null) {
            this.performAnalysis(this.givenModifiedDeploymentModelSpec, originalDeploymentModel);
        }
    }
    
    private void initialize() {
        super.initialize("Event Trace Analysis");
    }
    
    private void initializeControls() {
        this.setLayout(new BorderLayout());
        
        this.add(this.mainPanel.get(), BorderLayout.CENTER);
        this.add(this.toolBar.get(), BorderLayout.SOUTH);
    }
    
    private void initializeDefaults() {
        this.originalModelArea.get().setText(this.originalDeploymentModelSpec);
        
        if (this.givenModifiedDeploymentModelSpec != null) {
            this.modifiedModelArea.get().setText(this.givenModifiedDeploymentModelSpec);
        }
    }
    
    private JPanel createMainPanel() {
        var panel = new JPanel();        
        var layout = new SimpleGridBagLayout(panel, 2, 2);

        layout.add(this.originalModelPane.get(), 0, 0, 1, 1);        
        layout.add(this.modifiedModelPane.get(), 1, 0, 1, 1);
        layout.add(this.resultPane.get(), 0, 1, 2, 1);
        
        return panel;
    }
    
    private JScrollPane createOriginalModelPane() {
        var scrollPane = new JScrollPane(this.originalModelArea.get());
        
        scrollPane.setBorder(BorderFactory.createTitledBorder("Original Deployment Model"));
        
        return scrollPane;
    }
    
    private JTextArea createOriginalModelArea() {
        var textArea = new JTextArea();
        
        textArea.setFont(MONOSPACED_FONT);
        textArea.setEditable(false);
        
        return textArea;
    }
    
    private JScrollPane createModifiedModelPane() {
        var scrollPane = new JScrollPane(this.modifiedModelArea.get());
        
        scrollPane.setBorder(BorderFactory.createTitledBorder("Modified Deployment Model"));
        
        return scrollPane;
    }
    
    private JTextArea createModifiedModelArea() {
        var textArea = new JTextArea();
        
        textArea.setFont(MONOSPACED_FONT);
        
        return textArea;
    }
    
    private JToolBar createToolBar() {
        var toolBar = new JToolBar();
        
        toolBar.add(this.analyzeScenarioButton.get());
        toolBar.add(this.resetScenarioButton.get());
        
        return toolBar;
    }
    
    private JButton createAnalyzeScenarioButton() {
        var button = new JButton("Analyze Scenario");
        
        button.addActionListener(this::analyzeScenarioAction);
        
        return button;
    }
    
    private void analyzeScenarioAction(ActionEvent event) {
        this.performAnalysis(this.modifiedModelArea.get().getText(), this.originalDeploymentModel);
    }
    
    private void performAnalysis(String modifiedDeploymentModelSpec, DeploymentModel baseModel) {
        var modifiedDeploymentModel = new DeploymentModelReader().readModel(modifiedDeploymentModelSpec, baseModel);
        
        var analysis = new ConsistencyIssuesAnalysis();
        var originalTrace = this.trace;
        var rewrittenTrace = analysis.rewriteTrace(originalTrace, modifiedDeploymentModel);
        
        var originalTraceIssues = analysis.analyzeTrace(originalTrace, this.originalDeploymentModel);
        var rewrittenTraceIssues = analysis.analyzeTrace(rewrittenTrace, modifiedDeploymentModel);
        
        var diff = analysis.diffAnalyzerResults(originalTraceIssues, rewrittenTraceIssues, rewrittenTrace::obtainOriginalEvent);
        
        var spanTrace = new TraceBuilder().buildTrace(rewrittenTrace, modifiedDeploymentModel, Set.of());
        this.traceView.get().trace(spanTrace);
        
        var consistencyIssueViews = new ArrayList<ConsistencyIssueView>();
        createIssueViews(diff.newIssues(), "new", consistencyIssueViews::add);
        createIssueViews(diff.obsoleteIssues(), "obsolete", consistencyIssueViews::add);
        createIssueViews(diff.unchangedIssues(), "unchanged", consistencyIssueViews::add);
        
        Collections.sort(consistencyIssueViews);
        this.issuesTable.get().setModel(new ConsistencyIssuesTableModel(consistencyIssueViews));
        
        var writeChanges = new ArrayList<WriteChangeView>();
        createWriteChangeViews(diff.nowCommittedWrites(), "now committed", writeChanges::add);
        createWriteChangeViews(diff.nowRevertedWrites(), "now reverted", writeChanges::add);
        Collections.sort(writeChanges);
        
        this.writesTable.get().setModel(new WriteChangeTableModel(writeChanges));
    }
    
    private static void createIssueViews(Collection<ConsistencyIssue<?>> issues, String type, Consumer<ConsistencyIssueView> viewConsumer) {
        for (var issue : issues) {
            var view = new ConsistencyIssueView(issue.event().timestamp(), type, issue.event().getClass().getSimpleName(), issue.entity().type().name(), issue.entity().id());            
            viewConsumer.accept(view); 
        }
    }
    
    private static void createWriteChangeViews(Collection<EntityWriteEvent> events, String type, Consumer<WriteChangeView> viewConsumer) {
        for (var event : events) {
            var view = new WriteChangeView(event.timestamp(), type, event.entity().type().name(), event.entity().id());
            viewConsumer.accept(view);
        }
    }
    
    private JButton createResetScenarioButton() {
        var button = new JButton("Reset Scenario");
        
        button.addActionListener(this::resetScenarioAction);
        
        return button;
    }
    
    private void resetScenarioAction(ActionEvent event) {
        var textToSet = (this.givenModifiedDeploymentModelSpec == null) ? "" : this.givenModifiedDeploymentModelSpec;
        this.modifiedModelArea.get().setText(textToSet);
    }
    
    private JTabbedPane createResultPane() {
        var tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("Trace View", this.traceViewPane.get());
        tabbedPane.addTab("Consistency Issues", this.issuesTablePane.get());
        tabbedPane.addTab("Entity Writes", this.writesTablePane.get());
        
        return tabbedPane;
    }
    
    private JScrollPane createTraceViewPane() {
        return new JScrollPane(this.traceView.get());
    }
    
    private TraceViewComponent createTraceView() {
        return new TraceViewComponent();
    }
    
    private JScrollPane createIssuesTablePane() {
        return new JScrollPane(this.issuesTable.get());
    }
    
    private JTable createIssuesTable() {
        return new JTable();
    }
    
    private JScrollPane createWritesTablePane() {
        return new JScrollPane(this.writesTable.get());
    }
    
    private JTable createWritesTable() {
        return new JTable();
    }
    
    private record ConsistencyIssueView(long timestamp, String type, String description, String affectedEntityType, String affectedEntityId) implements Comparable<ConsistencyIssueView> {
        
        @Override
        public int compareTo(ConsistencyIssueView that) {
            return Long.compare(this.timestamp(), that.timestamp());
        }
        
    }
    
    private static class ConsistencyIssuesTableModel extends SimpleTableModel<ConsistencyIssueView> {
        
        private static final long serialVersionUID = 6131227781604948634L;
        
        private static final List<String> COLUMN_NAMES = List.of("Timestamp", "Type of Change");
        
        public ConsistencyIssuesTableModel(List<ConsistencyIssueView> values) {
            super(COLUMN_NAMES, values);
        }

        @Override
        protected Object fieldOf(ConsistencyIssueView object, int columnIndex) {
            return switch (columnIndex) {
            case 0 -> object.timestamp();
            case 1 -> object.type();
            default -> "";
            };
        }
        
    }
    
    private record WriteChangeView(long timestamp, String type, String affectedEntityType, String affectedEntityId) implements Comparable<WriteChangeView> {
        
        @Override
        public int compareTo(WriteChangeView that) {
            return Long.compare(this.timestamp(), that.timestamp());
        }
        
    }
    
    private static class WriteChangeTableModel extends SimpleTableModel<WriteChangeView> {
        
        private static final long serialVersionUID = 6131227781604948634L;
        
        private static final List<String> COLUMN_NAMES = List.of("Timestamp", "Type of Change");
        
        public WriteChangeTableModel(List<WriteChangeView> values) {
            super(COLUMN_NAMES, values);
        }

        @Override
        protected Object fieldOf(WriteChangeView object, int columnIndex) {
            return switch (columnIndex) {
            case 0 -> object.timestamp();
            case 1 -> object.type();
            default -> "";
            };
        }
        
    }
    
}
