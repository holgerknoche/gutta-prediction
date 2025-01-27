package gutta.prediction.ui.shape;

/**
 * Shape for an prepended overhead shape, i.e., overhead that occurs before a span.
 */
final class PrependedOverheadShape extends InvocationOverheadShape {

    private static final long serialVersionUID = 6916723154906612646L;

    public PrependedOverheadShape(int startX, int y, int endX) {
        int centerY = y + (HEIGHT / 2);

        this.moveTo(startX, y);
        this.lineTo(startX, (y + HEIGHT));
        this.moveTo(startX, centerY);
        this.lineTo(endX, centerY);
    }

}
