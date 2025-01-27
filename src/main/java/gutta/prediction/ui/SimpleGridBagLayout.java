package gutta.prediction.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;

/**
 * Utility class to facilitate the specification of a {@link GridBagLayout}.
 */
class SimpleGridBagLayout {
    
    private final GridBagLayout layout;        

    private final Container container;
    
    private final GridBagConstraints constraints = new GridBagConstraints();
    
    public SimpleGridBagLayout(Container container, int numberOfColumns, int numberOfRows) {
        this.container = container;
        this.layout = new GridBagLayout();
        
        container.setLayout(this.layout);
        
        this.constraints.fill = GridBagConstraints.BOTH;
        
        double[] columnWeights = new double[numberOfColumns];
        Arrays.fill(columnWeights, 1.0);
        this.layout.columnWeights = columnWeights;
        
        double[] rowWeights = new double[numberOfRows];
        Arrays.fill(rowWeights, 1.0);                
        this.layout.rowWeights = rowWeights;
    }
    
    public SimpleGridBagLayout fill(int fillType) {
        this.constraints.fill = fillType;
        return this;
    }
    
    public SimpleGridBagLayout columnWeights(double... weights) {
        this.layout.columnWeights = weights;
        return this;
    }
    
    public SimpleGridBagLayout rowWeights(double... weights) {
        this.layout.rowWeights = weights;
        return this;
    }
    
    public SimpleGridBagLayout add(Component component, int gridX, int gridY, int width, int height) {
        this.constraints.gridx = gridX;
        this.constraints.gridy = gridY;
        this.constraints.gridheight = height;
        this.constraints.gridwidth = width;

        this.layout.setConstraints(component, constraints);
        this.container.add(component);
        
        return this;
    }
    
}
