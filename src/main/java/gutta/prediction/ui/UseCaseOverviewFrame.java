package gutta.prediction.ui;

import gutta.prediction.analysis.overview.UseCaseOverviewAnalysis;
import gutta.prediction.event.codec.EventTraceDecoder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

class UseCaseOverviewFrame extends JFrame {

    private static final long serialVersionUID = 1827116057177051262L;

    private final InitializeOnce<JMenuBar> menuBar = new InitializeOnce<>(this::createMenuBar);

    private final InitializeOnce<JScrollPane> useCasesTablePane = new InitializeOnce<>(this::createUseCasesTablePane);

    private final InitializeOnce<JTable> useCasesTable = new InitializeOnce<>(this::createUseCasesTable);

    public UseCaseOverviewFrame() {
        this.initialize();
        this.initializeControls();
    }

    private void initialize() {
        this.setTitle("Use Case Overview");
        this.setSize(new Dimension(1024, 768));
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void initializeControls() {
        this.setJMenuBar(this.menuBar.get());

        this.setLayout(new BorderLayout());
        this.add(this.useCasesTablePane.get(), BorderLayout.CENTER);
    }

    private JMenuBar createMenuBar() {
        var menuBar = new JMenuBar();

        menuBar.add(this.createTracesMenu());

        return menuBar;
    }

    private JMenu createTracesMenu() {
        var tracesMenu = new JMenu("Traces");

        var loadMenuItem = new JMenuItem("Load...");
        loadMenuItem.addActionListener(this::loadTracesAction);
        tracesMenu.add(loadMenuItem);

        return tracesMenu;
    }

    private JScrollPane createUseCasesTablePane() {
        return new JScrollPane(this.useCasesTable.get());
    }

    private JTable createUseCasesTable() {
        var table = new JTable();

        return table;
    }

    private void loadTracesAction(ActionEvent event) {
        var openDialog = new FileDialog(this);
        openDialog.setMultipleMode(false);
        openDialog.setVisible(true);

        var selectedFiles = openDialog.getFiles();
        if (selectedFiles.length == 0) {
            return;
        } else {
            var selectedFile = selectedFiles[0];
            this.loadTracesFromFile(selectedFile);
        }
    }

    private void loadTracesFromFile(File file) {
        try (var inputStream = new FileInputStream(file)) {
            var eventTraces = new EventTraceDecoder().decodeTraces(inputStream);
            System.out.println(eventTraces.size() + " traces read from " + file);

            var results = new UseCaseOverviewAnalysis().analyzeTraces(eventTraces);
            this.refreshUseCaseTable(results);
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
            switch (column) {
            case 0:
                return "Use Case";
            case 1:
                return "# of Traces";
            case 2:
                return "Avg. Duration";
            case 3:
                return "Latency %";

            default:
                return "";
            }
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
