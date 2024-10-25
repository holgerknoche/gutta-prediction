package gutta.prediction.ui;

import gutta.prediction.analysis.overview.UseCaseOverviewAnalysis;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.dsl.DeploymentModelReader;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.codec.EventTraceDecoder;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import static javax.swing.JOptionPane.showMessageDialog;

class UseCaseOverviewFrame extends UIFrameTemplate {

    private static final long serialVersionUID = 1827116057177051262L;

    private final InitializeOnce<JMenuBar> menuBar = new InitializeOnce<>(this::createMenuBar);

    private final InitializeOnce<JScrollPane> useCasesTablePane = new InitializeOnce<>(this::createUseCasesTablePane);

    private final InitializeOnce<JTable> useCasesTable = new InitializeOnce<>(this::createUseCasesTable);
    
    private final InitializeOnce<JScrollPane> deploymentModelPane = new InitializeOnce<>(this::createDeploymentModelPane);
    
    private final InitializeOnce<JTextArea> deploymentModelArea = new InitializeOnce<>(this::createDeploymentModelArea);

    private Map<String, Collection<EventTrace>> tracesPerUseCase = new HashMap<>();
    
    private String deploymentModelSpec;
    
    private DeploymentModel deploymentModel;
    
    public UseCaseOverviewFrame(File tracesFile, File deploymentModelFile) {
        this.initialize();
        this.initializeControls();
        this.initializeDefaults(tracesFile, deploymentModelFile);
    }

    private void initialize() {
        super.initialize("Use Case Overview");
    }

    private void initializeControls() {
        this.setJMenuBar(this.menuBar.get());

        var layout = new SimpleGridBagLayout(this, 1, 2);
        
        layout.add(this.useCasesTablePane.get(), 0, 0, 1, 1);
        layout.add(this.deploymentModelPane.get(), 0, 1, 1, 1);
    }
    
    private void initializeDefaults(File tracesFile, File deploymentModelFile) {
        if (tracesFile != null && tracesFile.exists()) {
            this.loadTracesFromFile(tracesFile);
        }
        
        if (deploymentModelFile != null && deploymentModelFile.exists()) {
            this.loadDeploymentModelFromFile(deploymentModelFile);
        }
    }

    private JMenuBar createMenuBar() {
        var menuBar = new JMenuBar();

        menuBar.add(this.createTracesMenu());
        menuBar.add(this.createAnalysisMenu());

        return menuBar;
    }

    private JMenu createTracesMenu() {
        var tracesMenu = new JMenu("Traces");

        var loadTracesMenuItem = new JMenuItem("Load Traces...");
        loadTracesMenuItem.addActionListener(this::loadTracesAction);
        tracesMenu.add(loadTracesMenuItem);
        
        var loadModelMenuItem = new JMenuItem("Load Deployment Model...");
        loadModelMenuItem.addActionListener(this::loadDeploymentModelAction);
        tracesMenu.add(loadModelMenuItem);
        
        return tracesMenu;
    }
    
    private JMenu createAnalysisMenu() {
        var analysisMenu = new JMenu("Analysis");
        
        var latencyChangeMenuItem = new JMenuItem("Latency change analysis...");
        latencyChangeMenuItem.addActionListener(this::performLatencyAnalysisAction);
        analysisMenu.add(latencyChangeMenuItem);        
        
        var consistencyChangeMenuItem = new JMenuItem("Consistency change analysis...");
        consistencyChangeMenuItem.addActionListener(this::performConsistencyAnalysisAction);
        analysisMenu.add(consistencyChangeMenuItem);
        
        return analysisMenu;
    }

    private JScrollPane createUseCasesTablePane() {
        var pane = new JScrollPane(this.useCasesTable.get());
        
        pane.setBorder(BorderFactory.createTitledBorder("Use Cases"));
        
        return pane;
    }

    private JTable createUseCasesTable() {
        var table = new JTable();

        table.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {                    
                    UseCaseOverviewFrame.this.showTracesViewFrame(event);
                }
            }
            
        });
        
        return table;
    }
    
    private void showTracesViewFrame(MouseEvent event) {
        var table = this.useCasesTable.get();
        
        var row = table.rowAtPoint(event.getPoint());
        var useCaseName = (String) table.getValueAt(row, 0);
        var traces = this.tracesPerUseCase.get(useCaseName);
        
        var tracesFrame = new TracesViewFrame(useCaseName, this.deploymentModelSpec, this.deploymentModel, traces);
        tracesFrame.setVisible(true);
    }
    
    private JScrollPane createDeploymentModelPane() {
        var pane = new JScrollPane(this.deploymentModelArea.get());
        
        pane.setBorder(BorderFactory.createTitledBorder("Deployment Model"));
        
        return pane;
    }
    
    private JTextArea createDeploymentModelArea() {
        var textArea = new JTextArea();
        
        textArea.setFont(MONOSPACED_FONT);
        textArea.setEditable(false);
        
        var popupMenu = new JPopupMenu();
        var loadModelMenuItem = new JMenuItem("Load model...");        
        loadModelMenuItem.addActionListener(this::loadTracesAction);
        popupMenu.add(loadModelMenuItem);
        
        textArea.setComponentPopupMenu(popupMenu);
        
        return textArea;
    }

    private void loadTracesAction(ActionEvent event) {
        var selectedFile = this.loadFileWithDialog();
        
        selectedFile.ifPresent(this::loadTracesFromFile);
    }

    private void loadDeploymentModelAction(ActionEvent event) {
        var selectedFile = this.loadFileWithDialog();
        
        selectedFile.ifPresent(this::loadDeploymentModelFromFile);
    }
    
    private Optional<File> loadFileWithDialog() {
        var openDialog = new FileDialog(this);
        openDialog.setMultipleMode(false);
        openDialog.setVisible(true);

        var selectedFiles = openDialog.getFiles();
        if (selectedFiles.length == 0) {
            return Optional.empty();
        } else {
            return Optional.of(selectedFiles[0]);            
        }
    }
    
    private void loadTracesFromFile(File file) {
        try (var inputStream = new FileInputStream(file)) {
            var eventTraces = new EventTraceDecoder().decodeTraces(inputStream);
            
            this.tracesPerUseCase = UseCaseOverviewAnalysis.groupByUseCase(eventTraces);
            
            var results = new UseCaseOverviewAnalysis().analyzeTraces(eventTraces);            
            this.refreshUseCaseTable(results);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadDeploymentModelFromFile(File file) {
        try {
            var modelSpec = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            var model = new DeploymentModelReader().readModel(modelSpec);
            
            this.deploymentModelSpec = modelSpec;
            this.deploymentModel = model;
            
            this.deploymentModelArea.get().setText(modelSpec);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }            
    }

    private void refreshUseCaseTable(Map<String, UseCaseOverviewAnalysis.UseCaseOverview> useCaseOverviews) {
        var views = new ArrayList<UseCaseView>();

        for (var entry : useCaseOverviews.entrySet()) {
            var useCaseName = entry.getKey();
            var overview = entry.getValue();
            
            var view = new UseCaseView(useCaseName, overview.traces().size(), overview.averageDuration(), overview.latencyPercentage());
            views.add(view);
        }
        
        views.sort((view1, view2) -> view1.useCaseName().compareTo(view2.useCaseName()));
        
        this.useCasesTable.get().setModel(new UseCaseTableModel(views));
    }

    private void performLatencyAnalysisAction(ActionEvent event) {
        if (this.deploymentModel == null) {
            JOptionPane.showMessageDialog(this, "No deployment model loaded. Please load a deployment model first.");
        }
        
        var frame = new UseCaseLatencyAnalysisFrame(this.tracesPerUseCase, this.deploymentModelSpec, this.deploymentModel);
        frame.setVisible(true);
    }
    
    private void performConsistencyAnalysisAction(ActionEvent event) {
        if (this.deploymentModel == null) {
            showMessageDialog(this, "No deployment model loaded. Please load a deployment model first.");
        }
        
        var frame = new UseCaseConsistencyAnalysisFrame(this.tracesPerUseCase, this.deploymentModelSpec, this.deploymentModel);
        frame.setVisible(true);
    }
    
    private record UseCaseView(String useCaseName, int numberOfTraces, double averageDuration, double latencyPercentage) {
    }
    
    private static class UseCaseTableModel extends SimpleTableModel<UseCaseView> {

        private static final List<String> COLUMN_NAMES = List.of("Use Case", "# of Traces", "Avg. Duration", "Latency %");
        
        private static final long serialVersionUID = -5733710300186756473L;
        
        public UseCaseTableModel(List<UseCaseView> values) {
            super(COLUMN_NAMES, values);            
        }

        @Override
        protected Object fieldOf(UseCaseView object, int columnIndex) {
            return switch(columnIndex) {
            case 0 -> object.useCaseName();
            case 1 -> object.numberOfTraces();
            case 2 -> String.format("%.02f", object.averageDuration());
            case 3 -> String.format("%.02f", object.latencyPercentage());
            default -> "";
            };
        }
        
    }

}
