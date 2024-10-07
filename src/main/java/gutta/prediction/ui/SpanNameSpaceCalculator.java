package gutta.prediction.ui;

import gutta.prediction.span.Span;
import gutta.prediction.span.Trace;
import gutta.prediction.span.TraceElementVisitor;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

class SpanNameSpaceCalculator implements TraceElementVisitor<Void> {
    
    private final int topBottomBorderSize;
    
    private final int verticalDistanceBetweenSpans;
    
    private final FontMetrics textFontMetrics;
    
    private final Graphics graphics;
    
    private int maxX;
    
    private int maxY;
    
    public SpanNameSpaceCalculator(int topBottomBorderSize, int verticalDistanceBetweenSpans, FontMetrics textFontMetrics, Graphics graphics) {
        this.topBottomBorderSize = topBottomBorderSize;
        this.verticalDistanceBetweenSpans = verticalDistanceBetweenSpans;
        this.textFontMetrics = textFontMetrics;
        this.graphics = graphics;
    }
    
    public Dimension calulateSpaceForSpanNames(Trace trace) {
        this.maxX = 0;
        this.maxY = 0;
        
        trace.traverse(this);
        
        return new Dimension(this.maxX, this.maxY + (2 * this.topBottomBorderSize));
    }
    
    @Override
    public Void handleSpan(Span span) {
        if (!span.isRoot()) {
            this.maxY += this.verticalDistanceBetweenSpans;
        }
        
        var renderedNameBounds = this.textFontMetrics.getStringBounds(span.name(), this.graphics);
        var nameWidth = (int) renderedNameBounds.getMaxX();
        
        if (nameWidth > this.maxX) {
            this.maxX = nameWidth;
        }
        
        return null;
    }
    
}
