package gutta.prediction.ui;

import java.awt.Font;
import java.awt.Graphics2D;

class SpanNameShape implements DrawableShape {

    private static final Font TEXT_FONT = Font.decode(Font.SANS_SERIF);
    
    private final int x;
    
    private final int y;        
    
    private final String name;
    
    private boolean initialized = false;
    
    private int actualY;
    
    public SpanNameShape(int x, int y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
    }
    
    @Override
    public void drawOn(Graphics2D graphics) {
        if (!this.initialized) {
            var metrics = graphics.getFontMetrics(TEXT_FONT);
            var lineMetrics = metrics.getLineMetrics(this.name, graphics);

            // Center the text vertically
            var textAscent = (int) lineMetrics.getAscent();
            var textDescent = (int) lineMetrics.getDescent();
            var textHeight = textAscent + textDescent;
            
            var centerToBaseline = textAscent - (textHeight / 2);
            
            this.actualY = this.y + centerToBaseline;
            this.initialized = true;
        }
        
        graphics.setFont(TEXT_FONT);
        graphics.drawString(this.name, this.x, this.actualY);        
    }

}
