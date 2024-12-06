package gutta.prediction.ui.shape;

/**
 * Shape for an appended overhead shape, i.e., overhead that occurs after a span.
 */
final class AppendedOverheadShape extends InvocationOverheadShape {

    private static final long serialVersionUID = 820437604965312898L;

    public AppendedOverheadShape(int startX, int y, int endX) {
        int centerY = y + (HEIGHT / 2);

        this.moveTo(startX, centerY);
        this.lineTo(endX, centerY);
        this.moveTo(endX, y);
        this.lineTo(endX, (y + HEIGHT));
    }

}
