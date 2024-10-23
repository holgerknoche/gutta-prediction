package gutta.prediction.ui;

import gutta.prediction.analysis.overview.UseCaseOverviewAnalysis;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.dsl.DeploymentModelReader;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.codec.EventTraceDecoder;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

class UseCaseOverviewFrame extends UIFrameTemplate {

    private static final long serialVersionUID = 1827116057177051262L;

    private final InitializeOnce<JMenuBar> menuBar = new InitializeOnce<>(this::createMenuBar);

    private final InitializeOnce<JScrollPane> useCasesTablePane = new InitializeOnce<>(this::createUseCasesTablePane);

    private final InitializeOnce<JTable> useCasesTable = new InitializeOnce<>(this::createUseCasesTable);

    private Map<String, Collection<EventTrace>> tracesPerUseCase = new HashMap<>();
    
    private DeploymentModel deploymentModel;
    
    public UseCaseOverviewFrame() {
        this.initialize();
        this.initializeControls();
    }

    private void initialize() {
        super.initialize("Use Case Overview");
    }

    private void initializeControls() {
        this.setJMenuBar(this.menuBar.get());

        this.setLayout(new BorderLayout());
        this.add(this.useCasesTablePane.get(), BorderLayout.CENTER);
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
        
        return analysisMenu;
    }

    private JScrollPane createUseCasesTablePane() {
        return new JScrollPane(this.useCasesTable.get());
    }

    private JTable createUseCasesTable() {
        var table = new JTable();

        table.addMouseListener(new MouseBaseListener() {
            
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    if (UseCaseOverviewFrame.this.deploymentModel == null) {
                        JOptionPane.showMessageDialog(UseCaseOverviewFrame.this, "No deployment model loaded. Please load a deployment model first.");
                        return;
                    }
                    
                    var table = useCasesTable.get();
                    
                    var row = table.rowAtPoint(event.getPoint());
                    var useCaseName = (String) table.getValueAt(row, 0);
                    var traces = tracesPerUseCase.get(useCaseName);
                    
                    var tracesFrame = new TracesViewFrame(useCaseName, traces, UseCaseOverviewFrame.this.deploymentModel);
                    tracesFrame.setVisible(true);
                }
            }
            
        });
        
        return table;
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
        try (var inputStream = new FileInputStream(file)) {
            this.deploymentModel = new DeploymentModelReader().readModel(inputStream);            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshUseCaseTable(Map<String, UseCaseOverviewAnalysis.UseCaseOverview> useCaseOverviews) {
        this.useCasesTable.get().setModel(new UseCaseTableModel(useCaseOverviews));
    }

    private static class UseCaseTableModel extends AbstractTableModel {

        private static final long serialVersionUID = -5733710300186756473L;

        private final List<String> useCaseNames;

        private final Map<String, UseCaseOverviewAnalysis.UseCaseOverview> useCaseOverviews;

        public UseCaseTableModel(Map<String, UseCaseOverviewAnalysis.UseCaseOverview> useCaseOverviews) {
            var useCaseNames = useCaseOverviews.keySet().stream().collect(Collectors.toList());
            useCaseNames.sort(String::compareTo);

            this.useCaseNames = useCaseNames;
            this.useCaseOverviews = useCaseOverviews;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
        
        @Override
        public String getColumnName(int column) {
            return switch (column) {
            case 0 -> "Use Case";
            case 1 -> "# of Traces";
            case 2 -> "Avg. Duration";
            case 3 -> "Latency %";
            default -> "";
            };
        }

        @Override
        public int getRowCount() {
            return this.useCaseNames.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return this.useCaseNames.get(rowIndex);
            }

            var useCaseName = this.useCaseNames.get(rowIndex);
            var overview = this.useCaseOverviews.get(useCaseName);

            switch (columnIndex) {
            case 1:
                return overview.traces().size();

            case 2:
                return String.format("%.02f", overview.averageDuration());

            case 3:
                return String.format("%.02f", overview.latencyPercentage());

            default:
                return "";
            }
        }

    }

}
