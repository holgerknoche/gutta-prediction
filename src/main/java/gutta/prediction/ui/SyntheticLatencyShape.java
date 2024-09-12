package gutta.prediction.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

abstract class SyntheticLatencyShape extends Path2D.Float implements DrawableShape {

	private static final long serialVersionUID = -8198955856180731652L;
	
	protected static final int HEIGHT = 20;
	
	@Override
	public void drawOn(Graphics2D graphics) {
		graphics.setColor(Color.BLACK);
		graphics.draw(this);
	}
	
}
