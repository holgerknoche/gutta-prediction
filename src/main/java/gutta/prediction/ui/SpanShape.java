package gutta.prediction.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

class SpanShape extends RoundRectangle2D.Float implements DrawableShape {
	
	private static final long serialVersionUID = -6874493772369206074L;
	
	private static final Color GREENISH_BLUE = new Color(53, 128, 187);
	
	public SpanShape(int startX, int y, int endX) {
		super(startX, y, (endX - startX), 20, 10, 10);
	}
	
	@Override
	public void drawOn(Graphics2D graphics) {
		graphics.setColor(GREENISH_BLUE);
		graphics.fill(this);
		graphics.setColor(Color.BLACK);
		graphics.draw(this);
	}

}
