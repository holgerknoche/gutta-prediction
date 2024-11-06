package gutta.prediction.ui.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

class TransactionIssueShape extends Path2D.Float implements DrawableShape {

    private static final long serialVersionUID = 6282155971105338455L;

    private static final int HEIGHT = 10;

    private final IssueType type;

    public TransactionIssueShape(int xCenter, int yCenter, IssueType type) {
        this.type = type;

        var halfHeight = (HEIGHT / 2);

        this.moveTo((xCenter - halfHeight), yCenter);
        this.lineTo(xCenter, (yCenter - halfHeight));
        this.lineTo((xCenter + halfHeight), yCenter);
        this.lineTo(xCenter, (yCenter + halfHeight));
        this.lineTo((xCenter - halfHeight), yCenter);
    }

    @Override
    public void drawOn(Graphics2D graphics) {
        graphics.setColor(Color.BLACK);
        graphics.draw(this);
        graphics.setColor(type.getColor());
        graphics.fill(this);
    }

    public enum IssueType {
        /**
         * Marker color for a stale read issue.
         */
        STALE_READ(Color.YELLOW),
        /**
         * Marker color for a potential deadlock issue.
         */
        POTENTIAL_DEADLOCK(Color.CYAN),
        /**
         * Marker color for a conflicting write issue.
         */
        CONFLICTING_WRITE(Color.BLUE),
        /**
         * Marker color for cross-component accesses.
         */
        CROSS_COMPONENT_ACCESS(Color.LIGHT_GRAY),
        /**
         * Marker color for interleaved write issues.
         */
        INTERLEAVED_WRITE(new Color(55, 126, 71));
        ;

        private final Color color;

        private IssueType(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return this.color;
        }

    }

}
