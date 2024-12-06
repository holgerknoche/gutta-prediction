package gutta.prediction.ui.shape;

import java.awt.Graphics2D;

/**
 * Interface for shapes drawable on a {@link Graphics2D} object.
 */
public interface DrawableShape {

    /**
     * Draws this shape on the given graphics object.
     * 
     * @param graphics The graphics object to draw on
     */
    void drawOn(Graphics2D graphics);

}
