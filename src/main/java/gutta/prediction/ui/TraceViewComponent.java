package gutta.prediction.ui;

import gutta.prediction.span.Trace;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

class TraceViewComponent extends TraceComponent {

    private static final long serialVersionUID = 29828340270697690L;
    
    private static final int BORDER_WIDTH = 20;
    
    private static final int VERTICAL_DISTANCE_BETWEEN_SPANS = 60;
                
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
        // Calculate X offset (based on the longest span name)
        var spanNamesDimensions = new SpanNameSpaceCalculator(BORDER_WIDTH, VERTICAL_DISTANCE_BETWEEN_SPANS, this.getFontMetrics(TEXT_FONT), this.getGraphics()).calulateSpaceForSpanNames(trace);        
        var xOffset = spanNamesDimensions.width + (3 * BORDER_WIDTH);
                
        // Build the necessary shapes and repaint
        this.shapes = new SpanComponentsCreator(startTimestamp, xOffset, BORDER_WIDTH, VERTICAL_DISTANCE_BETWEEN_SPANS).createShapesFor(trace);
        
        // Calculate the required dimensions (two border widths to separate the names from the spans)
        var preferredXSize = spanNamesDimensions.width + (int) duration + (4 * BORDER_WIDTH);
        this.preferredSize = new Dimension(preferredXSize, spanNamesDimensions.height);
        
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
