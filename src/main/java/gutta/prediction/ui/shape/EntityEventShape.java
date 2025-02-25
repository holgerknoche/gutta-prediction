package gutta.prediction.ui.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/**
 * Shape for entity-related events, such as reads.
 */
class EntityEventShape extends Ellipse2D.Float implements DrawableShape {

    private static final long serialVersionUID = -4752863530156002616L;

    private static final int DIAMETER = 10;
    
    private static final int RADIUS = (DIAMETER / 2);

    private final EntityEventType type;
    
    /**
     * Creates a new shape with its center point at the given coordinates.
     * 
     * @param centerX The x coordinate of the shape's center
     * @param y The y coordinate of the shape's center
     * @param type The type of the event, which determines the fill color
     */
    public EntityEventShape(int centerX, int centerY, EntityEventType type) {
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
    
    /**
     * Enumeration of all possible entity event types.
     */
    public enum EntityEventType {
        
        /**
         * Represents a read access to an entity.
         */
        READ(Color.ORANGE),
        
        /**
         * Represents a write access to an entity.
         */
        WRITE(Color.MAGENTA);

        private final Color color;

        private EntityEventType(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return this.color;
        }

    }
    
}
