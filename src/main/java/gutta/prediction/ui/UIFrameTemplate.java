package gutta.prediction.ui;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;

abstract class UIFrameTemplate extends JFrame {

    protected static final Font MONOSPACED_FONT = Font.decode(Font.MONOSPACED);
    
    private static final long serialVersionUID = -1562184937247941209L;    
    
    protected void initialize(String title) {
        this.setTitle(title);
        this.setSize(new Dimension(1024, 768));
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);        
    }
        
}
