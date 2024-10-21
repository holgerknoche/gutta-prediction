package gutta.prediction.ui;

import java.awt.Dimension;

import javax.swing.JFrame;

public class UseCaseOverviewFrame extends JFrame {

    private static final long serialVersionUID = 1827116057177051262L;

    public UseCaseOverviewFrame() {
        this.initialize();
    }
    
    private void initialize() {
        this.setTitle("Use Case Overview");
        this.setSize(new Dimension(1024, 768));
    }
    
}
