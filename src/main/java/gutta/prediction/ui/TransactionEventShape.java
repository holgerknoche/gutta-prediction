package gutta.prediction.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

class TransactionEventShape extends Ellipse2D.Float implements DrawableShape {

    private static final long serialVersionUID = 2023398039429544762L;

    private static final int DIAMETER = 10;
    
    private static final int RADIUS = (DIAMETER / 2);

    private final EventType type;

    /**
     * Creates a new shape with its center point at the given coordinates.
     * 
     * @param centerX The x coordinate of the shape's center
     * @param y The y coordinate of the shape's center
     * @param type The type of the event, which determines the fill color
     */
    public TransactionEventShape(int centerX, int centerY, EventType type) {
        super((centerX - RADIUS), (centerY - RADIUS), DIAMETER, DIAMETER);

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

    public enum EventType {
        READ(Color.WHITE), WRITE(Color.LIGHT_GRAY), STALE_READ(Color.YELLOW), POTENTIAL_DEADLOCK(Color.CYAN), CONFLICTING_WRITE(Color.BLUE), COMMIT(Color.GREEN), ABORT(Color.RED);

        private final Color color;

        private EventType(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return this.color;
        }

    }

}
