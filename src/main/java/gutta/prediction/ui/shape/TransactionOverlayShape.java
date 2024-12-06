package gutta.prediction.ui.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

class TransactionMarkerShape extends Rectangle2D.Float implements DrawableShape {

    private static final long serialVersionUID = -7055322866818518165L;

    private static final Color FRIENDLY_GREEN = new Color(154, 255, 154);

    private static final Color FRIENDLY_RED = new Color(255, 154, 154);

    private final TransactionState state;

    public TransactionMarkerShape(int startX, int y, int endX, TransactionState state) {
        super(startX, y, (endX - startX), 40);

        this.state = state;
    }

    @Override
    public void drawOn(Graphics2D graphics) {
        Color color = this.state.getColor();

        graphics.setColor(color);
        graphics.fill(this);
    }

    public enum TransactionState {
        CLEAN(FRIENDLY_GREEN), DIRTY(FRIENDLY_RED), SUSPENDED(Color.LIGHT_GRAY);

        private final Color color;

        private TransactionState(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return this.color;
        }

    }

}
