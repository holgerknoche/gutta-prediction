package gutta.prediction.ui.shape;

import java.util.ArrayList;
import java.util.List;

class LayeredShapes {
    
    private final int numberOfLayers;
    
    private final List<List<DrawableShape>> layers;
    
    public LayeredShapes(int numberOfLayers) {
        var layers = new ArrayList<List<DrawableShape>>(numberOfLayers);
        
        for (int layerIndex = 0; layerIndex < numberOfLayers; layerIndex++) {
            layers.add(new ArrayList<DrawableShape>());
        }
        
        this.numberOfLayers = numberOfLayers;
        this.layers = layers;
    }
    
    public void addShape(int layer, DrawableShape shape) {
        if (layer >= this.numberOfLayers) {
            throw new IllegalArgumentException("Invalid layer " + layer + ", must be between 0 and " + this.numberOfLayers + ".");
        }
        
        if (shape != null) {
            var shapesInLayer = this.layers.get(layer);
            shapesInLayer.add(shape);
        }
    }
    
    private int numberOfShapes() {
        var numberOfShapes = 0;
        
        for (var layer : this.layers) {
            numberOfShapes += layer.size();
        }
        
        return numberOfShapes;
    }
    
    public List<DrawableShape> flatten() {
        var flattenedShapes = new ArrayList<DrawableShape>(this.numberOfShapes());
        
        for (var layer : this.layers) {
            flattenedShapes.addAll(layer);
        }
        
        return flattenedShapes;
    }

}
