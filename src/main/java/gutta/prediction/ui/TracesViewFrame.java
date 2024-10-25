package gutta.prediction.ui;

import gutta.prediction.event.EventTrace;
import gutta.prediction.span.ObservedTraceBuilder;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import static gutta.prediction.analysis.overview.UseCaseOverviewAnalysis.determineDuration;
import static gutta.prediction.analysis.overview.UseCaseOverviewAnalysis.determineLatency;

class TracesViewFrame extends UIFrameTemplate {

    private static final long serialVersionUID = 1786288002124789312L;        
    
    private final InitializeOnce<JScrollPane> tracesListPane = new InitializeOnce<>(this::createTracesListPane);
    
    private final InitializeOnce<JTable> tracesTable = new InitializeOnce<>(this::createTracesTable);
    
    private final InitializeOnce<JScrollPane> traceViewPane = new InitializeOnce<>(this::createTraceViewPane);
    
    private final InitializeOnce<TraceViewComponent> traceView = new InitializeOnce<>(this::createTraceView);
        
    private final List<TraceView> traceViews;
    
    public TracesViewFrame(String useCaseName, Collection<EventTrace> traces) {
        this.traceViews = buildViews(traces);
        
        this.initialize(useCaseName);
        this.initializeControls();
        this.initializeData();
    }
    
    private static List<TraceView> buildViews(Collection<EventTrace> traces) {
        var views = new ArrayList<TraceView>(traces.size());
        
        var traceNumber = 0;
        for (var trace : traces) {
            var duration = determineDuration(trace);
            var latency = determineLatency(trace);
            
            var latencyPercentage = (duration > 0) ? (double) latency / (double) duration : 0.0;
            
            var view = new TraceView("Trace #" + traceNumber, duration, latencyPercentage, trace);
            views.add(view);
            
            traceNumber++;
        }
        
        return views;
    }
    
    protected void initialize(String useCaseName) {
        super.initialize("Traces for '" + useCaseName + "'");
    }
    
    private void initializeControls() {
        var layout = new SimpleGridBagLayout(this, 1, 2);
                
        layout.add(this.tracesListPane.get(), 0, 0, 1, 1);
        layout.add(this.traceViewPane.get(), 0, 1, 1, 1);
    }
    
    private void initializeData() {
        this.tracesTable.get().setModel(new TracesListModel(this.traceViews));
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
    
    private class TracesListModel extends SimpleTableModel<TraceView> {

        private static final long serialVersionUID = -16840755840867050L;

        private static final List<String> COLUMN_NAMES = List.of("Trace #", "Duration", "Latency %");
        
        public TracesListModel(List<TraceView> values) {
            super(COLUMN_NAMES, values);
        }
        
        @Override
        protected Object fieldOf(TraceView object, int columnIndex) {
            return switch (columnIndex) {
            case 0 -> object.name();
            case 1 -> object.duration();
            case 2 -> String.format("%.02f", object.latencyPercentage());
            default -> "";
            };
        }
        
    }
    
    private record TraceView(String name, long duration, double latencyPercentage, EventTrace trace) {                
    }

}
