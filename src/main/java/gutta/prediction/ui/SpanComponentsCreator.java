package gutta.prediction.ui;

import gutta.prediction.span.CleanTransactionOverlay;
import gutta.prediction.span.DirtyTransactionOverlay;
import gutta.prediction.span.LatencyOverlay;
import gutta.prediction.span.Span;
import gutta.prediction.span.SuspendedTransactionOverlay;
import gutta.prediction.span.Trace;
import gutta.prediction.span.TraceElementVisitor;
import gutta.prediction.span.TransactionOverlay;
import gutta.prediction.ui.TransactionMarkerShape.TransactionState;

import java.util.List;

class SpanComponentsCreator implements TraceElementVisitor<Void> {    

    private static final int NUMBER_OF_LAYERS = 4;
    
    private static final int OVERLAYS_LAYER = 0;
    
    private static final int SPANS_LAYER = 1;
    
    private static final int EVENTS_LAYER = 2;
    
    private static final int TEXT_LAYER = 3;
    
    private final long startTimestamp;
    
    private final int xOffset;
    
    private final int borderWidth;
    
    private final int verticalDistanceBetweenSpans;
    
    private final LayeredShapes shapes = new LayeredShapes(NUMBER_OF_LAYERS);
    
    private int currentY;
    
    private Span currentSpan;
    
    public SpanComponentsCreator(long startTimestamp, int xOffset, int borderWidth, int verticalDistanceBetweenSpans) {
        this.startTimestamp = startTimestamp;
        this.xOffset = xOffset;
        this.borderWidth = borderWidth;
        this.verticalDistanceBetweenSpans = verticalDistanceBetweenSpans;
    }
    
    public List<DrawableShape> createShapesFor(Trace trace) {
        this.currentY = this.borderWidth;
        
        trace.traverse(this);

        return this.shapes.flatten();
    }

    private int convertTimestampToXPosition(long timestamp) {
        return (int) (timestamp - this.startTimestamp) + this.xOffset;
    }
    
    @Override
    public Void handleSpan(Span span) {
        var xStart = this.convertTimestampToXPosition(span.startTimestamp());
        var xEnd = this.convertTimestampToXPosition(span.endTimestamp());               
        
        if (!span.isRoot()) {
            this.currentY += this.verticalDistanceBetweenSpans;
        }
        
        var spanNameShape = new SpanNameShape(this.borderWidth, (this.currentY + (SpanShape.HEIGHT / 2)), span.name());
        this.shapes.addShape(TEXT_LAYER, spanNameShape);
        
        var spanShape = new SpanShape(xStart, this.currentY, xEnd);
        this.shapes.addShape(SPANS_LAYER, spanShape);
        
        this.currentSpan = span;
        
        return null;
    }
    
    private Void handleTransactionOverlay(TransactionOverlay overlay, TransactionState state) {
        var xStart = this.convertTimestampToXPosition(overlay.startTimestamp());
        var xEnd = this.convertTimestampToXPosition(overlay.endTimestamp());
        
        var overlayShape = new TransactionMarkerShape(xStart, (this.currentY - 10), xEnd, state);
        this.shapes.addShape(OVERLAYS_LAYER, overlayShape);
        
        return null;
    }
    
    @Override
    public Void handleCleanTransactionOverlay(CleanTransactionOverlay overlay) {
        return this.handleTransactionOverlay(overlay, TransactionState.CLEAN);
    }
    
    @Override
    public Void handleDirtyTransactionOverlay(DirtyTransactionOverlay overlay) {
        return this.handleTransactionOverlay(overlay, TransactionState.DIRTY);
    }
    
    @Override
    public Void handleSuspendedTransactionOverlay(SuspendedTransactionOverlay overlay) {
        return this.handleTransactionOverlay(overlay, TransactionState.SUSPENDED);
    }
    
    @Override
    public Void handleLatencyOverlay(LatencyOverlay overlay) {
        var xStart = this.convertTimestampToXPosition(overlay.startTimestamp());
        var xEnd = this.convertTimestampToXPosition(overlay.endTimestamp());
        
        DrawableShape shape;
        if (overlay.startTimestamp() < this.currentSpan.startTimestamp()) {
            // If the overlay starts before the current span, it is a prepended latency
            shape = new PrependedLatencyShape(xStart, this.currentY, xEnd);
        } else {
            // Otherwise, it is an appended latency
            shape = new AppendedLatencyShape(xStart, this.currentY, xEnd);
        }
        
        this.shapes.addShape(SPANS_LAYER,shape);
        
        return null;
    }
    
}
