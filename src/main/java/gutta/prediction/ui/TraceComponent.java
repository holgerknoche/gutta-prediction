package gutta.prediction.ui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.LineMetrics;

import javax.swing.JComponent;

abstract class TraceComponent extends JComponent {
    
    private static final long serialVersionUID = 7164581631239551683L;

    private static final Font TEXT_FONT = Font.decode(Font.SANS_SERIF);

    @Override
    protected final void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        this.paint(graphics2D);
    }

    protected abstract void paint(Graphics2D graphics);

    protected void drawCentered(String text, int x, int y, Graphics2D graphics) {
        FontMetrics metrics = this.getFontMetrics(TEXT_FONT);
        LineMetrics lineMetrics = metrics.getLineMetrics(text, graphics);

        graphics.setFont(TEXT_FONT);
        graphics.drawString(text, x, y + (lineMetrics.getHeight() / 2));
    }

}
