package gutta.prediction.ui;

import gutta.prediction.analysis.consistency.PotentialDeadlockIssue;
import gutta.prediction.analysis.consistency.StaleReadIssue;
import gutta.prediction.domain.Entity;
import gutta.prediction.domain.EntityType;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.span.CleanTransactionOverlay;
import gutta.prediction.span.ConsistencyIssueEvent;
import gutta.prediction.span.DirtyTransactionOverlay;
import gutta.prediction.span.LatencyOverlay;
import gutta.prediction.span.Span;
import gutta.prediction.span.SuspendedTransactionOverlay;
import gutta.prediction.span.Trace;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class SpanViewFrame extends JFrame {

    private static final long serialVersionUID = -2692263064974192078L;
    
    private JPanel mainPanel;
    
    private JScrollPane tracePane;

    private TraceViewComponent traceViewComponent;
    
    public SpanViewFrame() {
        this.initialize();
        this.initializeDefaults();
    }

    private void initialize() {
        this.setTitle("Span View Test");
        this.setSize(new Dimension(1280, 768));

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        this.setLayout(new BorderLayout());
        this.add(this.getMainPanel(), BorderLayout.CENTER);
    }
    
    private void initializeDefaults() {
        var trace = this.buildDemoTrace();
        this.getTraceViewComponent().trace(trace);
    }
    
    private Trace buildDemoTrace() {
        var rootSpan = new Span("Root", 100, null);
        rootSpan.endTimestamp(1000);
        
        var childSpan = new Span("Child1", 300, rootSpan);
        childSpan.endTimestamp(900);
        childSpan.addOverlay(new CleanTransactionOverlay(400, 500));
        childSpan.addOverlay(new SuspendedTransactionOverlay(500, 700, false));
        childSpan.addOverlay(new CleanTransactionOverlay(700, 750));
        childSpan.addOverlay(new DirtyTransactionOverlay(750, 850));
        
        var entityType = new EntityType("type");
        var entity = new Entity(entityType, "1234");
        
        childSpan.addEvent(new ConsistencyIssueEvent(new StaleReadIssue(entity, new EntityReadEvent(0, 790, null, entity))));
        childSpan.addEvent(new ConsistencyIssueEvent(new PotentialDeadlockIssue(entity, new EntityReadEvent(0, 820, null, entity))));
        
        var secondChildSpan = new Span("Child2", 550, childSpan);
        secondChildSpan.endTimestamp(680);
        secondChildSpan.addOverlay(new LatencyOverlay(500, 550));
        secondChildSpan.addOverlay(new LatencyOverlay(680, 700));
        
        return new Trace(1234, "Span", rootSpan);
    }
    
    private JPanel getMainPanel() {
        if (this.mainPanel == null) {
            this.mainPanel = new JPanel();
            
            this.mainPanel.setLayout(new BoxLayout(this.mainPanel, BoxLayout.Y_AXIS));
            this.mainPanel.add(this.getTracePane());
        }
        
        return this.mainPanel;
    }
    
    private JScrollPane getTracePane() {
        if (this.tracePane == null) {
            this.tracePane = new JScrollPane();
            
            this.tracePane.setViewportView(this.getTraceViewComponent());
        }
        
        return this.tracePane;
    }
    
    private TraceViewComponent getTraceViewComponent() {
        if (this.traceViewComponent == null) {
            this.traceViewComponent = new TraceViewComponent();
        }
        
        return this.traceViewComponent;
    }

}
