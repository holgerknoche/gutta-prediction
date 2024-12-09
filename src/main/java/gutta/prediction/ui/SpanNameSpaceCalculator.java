package gutta.prediction.ui;

import gutta.prediction.span.Span;
import gutta.prediction.span.Trace;
import gutta.prediction.span.TraceElementVisitor;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

/**
 * Utility class to determine the necessary space for the span names in the given trace.
 */
class SpanNameSpaceCalculator implements TraceElementVisitor<Void> {

    private final int topBottomBorderSize;

    private final int verticalDistanceBetweenSpans;

    private final FontMetrics textFontMetrics;

    private final Graphics graphics;

    private int maxX;

    private int maxY;

    /**
     * Creates a new space calculator using the given data and dependencies.
     * 
     * @param topBottomBorderSize          The desired border size at the top and the bottom (in pixels)
     * @param verticalDistanceBetweenSpans The vertical distance between spans (in pixels)
     * @param textFontMetrics              The font metrics of the desired font
     * @param graphics                     The graphics to draw on
     */
    public SpanNameSpaceCalculator(int topBottomBorderSize, int verticalDistanceBetweenSpans, FontMetrics textFontMetrics, Graphics graphics) {
        this.topBottomBorderSize = topBottomBorderSize;
        this.verticalDistanceBetweenSpans = verticalDistanceBetweenSpans;
        this.textFontMetrics = textFontMetrics;
        this.graphics = graphics;
    }

    /**
     * Calculates the required space for the span names in the given trace.
     * 
     * @param trace The trace to display
     * @return The dimension of the required rectangle to draw the span names
     */
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
