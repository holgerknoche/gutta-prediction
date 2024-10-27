package gutta.prediction.ui.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

class TransactionEventShape extends Rectangle2D.Float implements DrawableShape {

    private static final long serialVersionUID = 2023398039429544762L;

    private static final int SIZE = 10;
        
    private final TransactionEventType type;

    /**
     * Creates a new shape with its center point at the given coordinates.
     * 
     * @param centerX The x coordinate of the shape's center
     * @param y The y coordinate of the shape's center
     * @param type The type of the event, which determines the fill color
     */
    public TransactionEventShape(int centerX, int centerY, TransactionEventType type) {
        super((centerX - (SIZE / 2)), (centerY - (SIZE / 2)), SIZE, SIZE);

        this.type = type;
    }

    @Override
    public void drawOn(Graphics2D graphics) {
        Color color = this.type.getColor();

        graphics.setColor(color);
        graphics.fill(this);
        graphics.setColor(Color.BLACK);
        graphics.draw(this);
    }

    public enum TransactionEventType {
        START(Color.WHITE), COMMIT(Color.GREEN), IMPLICIT_ABORT(Color.PINK), ABORT(Color.RED);

        private final Color color;

        private TransactionEventType(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return this.color;
        }

    }

}
