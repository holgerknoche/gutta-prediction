package gutta.prediction.ui;

import gutta.prediction.event.EventTrace;
import gutta.prediction.span.ObservedTraceBuilder;

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

class TracesViewFrame extends UIFrameTemplate {

    private static final long serialVersionUID = 1786288002124789312L;        
    
    private final InitializeOnce<JScrollPane> tracesListPane = new InitializeOnce<>(this::createTracesListPane);
    
    private final InitializeOnce<JTable> tracesTable = new InitializeOnce<>(this::createTracesTable);
    
    private final InitializeOnce<JScrollPane> traceViewPane = new InitializeOnce<>(this::createTraceViewPane);
    
    private final InitializeOnce<TraceViewComponent> traceView = new InitializeOnce<>(this::createTraceView);
        
    private final List<TraceView> traceViews;
    
    public TracesViewFrame(String useCaseName, Collection<EventTrace> traces) {
        this.traceViews = traces.stream().map(TraceView::new).collect(Collectors.toList());
        
        this.initialize(useCaseName);
        this.initializeControls();
        this.initializeData();
    }
    
    protected void initialize(String useCaseName) {
        super.initialize("Traces for '" + useCaseName + "'");
    }
    
    private void initializeControls() {
        var layout = new GridBagLayoutBuilder(this, 1, 2);
                
        layout.add(this.tracesListPane.get(), 0, 0, 1, 1);
        layout.add(this.traceViewPane.get(), 0, 1, 1, 1);
    }
    
    private void initializeData() {
        this.tracesTable.get().setModel(new TracesListModel());
    }
    
    private JScrollPane createTracesListPane() {
        return new JScrollPane(this.tracesTable.get());
    }
    
    private JTable createTracesTable() {
        var table = new JTable();
                
        table.addMouseListener(new MouseBaseListener() {
            
            @Override
            public void mouseClicked(MouseEvent event) {
                var table = TracesViewFrame.this.tracesTable.get();
                var rowIndex = table.rowAtPoint(event.getPoint());
                
                var view = TracesViewFrame.this.traceViews.get(rowIndex);
                var trace = view.trace();
                
                var spanTrace = new ObservedTraceBuilder(trace).buildTrace();
                TracesViewFrame.this.traceView.get().trace(spanTrace);
            }
            
        });
        
        return table;
    }
    
    private JScrollPane createTraceViewPane() {
        return new JScrollPane(this.traceView.get());
    }
    
    private TraceViewComponent createTraceView() {
        return new TraceViewComponent();
    }
    
    private class TracesListModel extends AbstractTableModel {

        private static final long serialVersionUID = -16840755840867050L;

        @Override
        public int getRowCount() {
            return TracesViewFrame.this.traceViews.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }
        
        @Override
        public String getColumnName(int column) {
            return switch (column) {
            case 0 -> "Trace #";
            case 1 -> "Duration";
            case 2 -> "Latency %";
            default -> "";
            };
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return "Trace #" + (rowIndex + 1);
            } else {
                return "";
            }
        }
        
    }
    
    private record TraceView(EventTrace trace) {                
        
    }

}
