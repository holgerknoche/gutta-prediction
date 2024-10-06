package gutta.prediction.ui;

import gutta.prediction.span.Trace;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

class TraceViewComponent extends TraceComponent {

    private static final long serialVersionUID = 29828340270697690L;
            
    private Dimension preferredSize;
    
    private List<DrawableShape> shapes = new ArrayList<>();
    
    public void trace(Trace trace) {
        if (trace == null) {
            // Delete all shapes
            this.shapes = new ArrayList<>();
            this.preferredSize = null;
            return;
        }
        
        var startTimestamp = trace.startTimestamp();
        var endTimestamp = trace.endTimestamp();
        
        // TODO Use the duration to calculate the necessary size / zoom factor
        var duration = (endTimestamp - startTimestamp);
        // TODO Calculate X offset (based on the longest span name)
        var xOffset = 100;
                
        // Build the necessary shapes and repaint
        this.shapes = new SpanComponentsCreator(startTimestamp, xOffset).createShapesFor(trace);
        
        // TODO Calculate the required dimensions
        this.preferredSize = new Dimension((int) duration, 768);
        
        this.repaint();
    }
    
    @Override
    public Dimension getPreferredSize() {
        return this.preferredSize;
    }
    
    @Override
    protected void paint(Graphics2D graphics) {
        this.shapes.forEach(shape -> shape.drawOn(graphics));
    }
    
}
