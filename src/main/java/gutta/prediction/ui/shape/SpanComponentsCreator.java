package gutta.prediction.ui.shape;

import gutta.prediction.analysis.consistency.ConsistencyIssue;
import gutta.prediction.analysis.consistency.ConsistencyIssueVisitor;
import gutta.prediction.analysis.consistency.CrossComponentAccessIssue;
import gutta.prediction.analysis.consistency.InterleavedWriteIssue;
import gutta.prediction.analysis.consistency.PotentialDeadlockIssue;
import gutta.prediction.analysis.consistency.StaleReadIssue;
import gutta.prediction.analysis.consistency.WriteConflictIssue;
import gutta.prediction.span.CleanTransactionOverlay;
import gutta.prediction.span.ConsistencyIssueEvent;
import gutta.prediction.span.DirtyTransactionOverlay;
import gutta.prediction.span.EntityEvent;
import gutta.prediction.span.OverheadOverlay;
import gutta.prediction.span.Span;
import gutta.prediction.span.SuspendedTransactionOverlay;
import gutta.prediction.span.Trace;
import gutta.prediction.span.TraceElementVisitor;
import gutta.prediction.span.TransactionEvent;
import gutta.prediction.span.TransactionOverlay;
import gutta.prediction.ui.shape.EntityEventShape.EntityEventType;
import gutta.prediction.ui.shape.TransactionEventShape.TransactionEventType;
import gutta.prediction.ui.shape.TransactionIssueShape.IssueType;
import gutta.prediction.ui.shape.TransactionOverlayShape.TransactionState;

import java.util.List;

/**
 * A {@link SpanComponentsCreator} builds graphical components (in particular, {@linkplain DrawableShape shapes}) from a span trace for visualization.
 */
public class SpanComponentsCreator implements TraceElementVisitor<Void> {

    private static final int NUMBER_OF_LAYERS = 4;

    private static final int OVERLAYS_LAYER = 0;

    private static final int SPANS_LAYER = 1;

    private static final int EVENTS_LAYER = 2;

    private static final int TEXT_LAYER = 3;

    private static final int HALF_SPAN_HEIGHT = 10;

    private final long startTimestamp;

    private final int xOffset;

    private final int borderWidth;

    private final int verticalDistanceBetweenSpans;

    private final LayeredShapes shapes = new LayeredShapes(NUMBER_OF_LAYERS);

    private final TransactionMarkerTypeChooser markerTypeChooser = new TransactionMarkerTypeChooser();

    private int currentY;

    private Span currentSpan;

    /**
     * Creates a new components creator using the given data.
     * 
     * @param startTimestamp               The start timestamp to use
     * @param xOffset                      The x offset at which to draw shapes (to leave room for labels)
     * @param borderWidth                  The border width in pixels
     * @param verticalDistanceBetweenSpans The vertical distance between spans in pixels
     */
    public SpanComponentsCreator(long startTimestamp, int xOffset, int borderWidth, int verticalDistanceBetweenSpans) {
        this.startTimestamp = startTimestamp;
        this.xOffset = xOffset;
        this.borderWidth = borderWidth;
        this.verticalDistanceBetweenSpans = verticalDistanceBetweenSpans;
    }

    /**
     * Creates the shapes for the given trace.
     * 
     * @param trace The trace to create the shapes for
     * @return The list of shapes for the trace
     */
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

        var overlayShape = new TransactionOverlayShape(xStart, (this.currentY - 10), xEnd, state);
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

    private IssueType determineIssueTypeFor(ConsistencyIssue<?> issue) {
        return issue.accept(this.markerTypeChooser);
    }

    @Override
    public Void handleConsistencyIssueEvent(ConsistencyIssueEvent event) {
        var xPosition = this.convertTimestampToXPosition(event.timestamp());
        var issueType = this.determineIssueTypeFor(event.issue());

        var markerShape = new TransactionIssueShape(xPosition, (this.currentY + 10), issueType);
        this.shapes.addShape(EVENTS_LAYER, markerShape);

        return null;
    }

    @Override
    public Void handleOverheadOverlay(OverheadOverlay overlay) {
        var xStart = this.convertTimestampToXPosition(overlay.startTimestamp());
        var xEnd = this.convertTimestampToXPosition(overlay.endTimestamp());

        DrawableShape shape;
        if (overlay.startTimestamp() < this.currentSpan.startTimestamp()) {
            // If the overlay starts before the current span, it is a prepended overhead
            shape = new PrependedOverheadShape(xStart, this.currentY, xEnd);
        } else {
            // Otherwise, it is an appended overhead
            shape = new AppendedOverheadShape(xStart, this.currentY, xEnd);
        }

        this.shapes.addShape(SPANS_LAYER, shape);

        return null;
    }

    @Override
    public Void handleEntityEvent(EntityEvent event) {
        var xPosition = this.convertTimestampToXPosition(event.timestamp());

        var shapeType = switch (event.accessType()) {
        case READ -> EntityEventType.READ;
        case WRITE -> EntityEventType.WRITE;
        };

        var shape = new EntityEventShape(xPosition, (this.currentY + HALF_SPAN_HEIGHT), shapeType);
        this.shapes.addShape(EVENTS_LAYER, shape);

        return null;
    }

    @Override
    public Void handleTransactionEvent(TransactionEvent event) {
        var xPosition = this.convertTimestampToXPosition(event.timestamp());

        var shapeType = switch (event.type()) {
        case START -> TransactionEventType.START;
        case IMPLICIT_ABORT -> TransactionEventType.IMPLICIT_ABORT;
        case EXPLICIT_ABORT -> TransactionEventType.ABORT;
        case COMMIT -> TransactionEventType.COMMIT;
        };

        var shape = new TransactionEventShape(xPosition, (this.currentY + HALF_SPAN_HEIGHT), shapeType);
        this.shapes.addShape(EVENTS_LAYER, shape);

        return null;
    }

    /**
     * Visitor to choose the appropriate issue type for a consistency issue.
     */
    private static class TransactionMarkerTypeChooser implements ConsistencyIssueVisitor<IssueType> {

        @Override
        public IssueType handlePotentialDeadlockIssue(PotentialDeadlockIssue issue) {
            return IssueType.POTENTIAL_DEADLOCK;
        }

        @Override
        public IssueType handleStaleReadIssue(StaleReadIssue issue) {
            return IssueType.STALE_READ;
        }

        @Override
        public IssueType handleWriteConflictIssue(WriteConflictIssue issue) {
            return IssueType.CONFLICTING_WRITE;
        }

        @Override
        public IssueType handleCrossComponentAccessIssue(CrossComponentAccessIssue issue) {
            return IssueType.CROSS_COMPONENT_ACCESS;
        }

        @Override
        public IssueType handleInterleavedWriteIssue(InterleavedWriteIssue issue) {
            return IssueType.INTERLEAVED_WRITE;
        }

    }

}
