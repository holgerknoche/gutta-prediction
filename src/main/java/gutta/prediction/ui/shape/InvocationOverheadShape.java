package gutta.prediction.ui.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

/**
 * Superclass for invocation overhead.
 */
abstract sealed class InvocationOverheadShape extends Path2D.Float implements DrawableShape permits AppendedOverheadShape, PrependedOverheadShape {

    private static final long serialVersionUID = -8198955856180731652L;

    protected static final int HEIGHT = 20;

    @Override
    public void drawOn(Graphics2D graphics) {
        graphics.setColor(Color.BLACK);
        graphics.draw(this);
    }

}
