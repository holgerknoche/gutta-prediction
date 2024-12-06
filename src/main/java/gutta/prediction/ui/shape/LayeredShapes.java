package gutta.prediction.ui.shape;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to assign shapes to layers, so that higher layers are drawn on top of lower layers.
 */
class LayeredShapes {

    private final int numberOfLayers;

    private final List<List<DrawableShape>> layers;

    /**
     * Creates a new set of layers.
     * 
     * @param numberOfLayers The number of layers
     */
    public LayeredShapes(int numberOfLayers) {
        var layers = new ArrayList<List<DrawableShape>>(numberOfLayers);

        for (int layerIndex = 0; layerIndex < numberOfLayers; layerIndex++) {
            layers.add(new ArrayList<DrawableShape>());
        }

        this.numberOfLayers = numberOfLayers;
        this.layers = layers;
    }

    /**
     * Adds the given shape to the given layer.
     * 
     * @param layer The index of the layer to add the shape to
     * @param shape The shape to add
     */
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

    /**
     * Flattens the shapes so that those on higher layers are behind those from lower layers.
     * 
     * @return The list of shapes
     */
    public List<DrawableShape> flatten() {
        var flattenedShapes = new ArrayList<DrawableShape>(this.numberOfShapes());

        for (var layer : this.layers) {
            flattenedShapes.addAll(layer);
        }

        return flattenedShapes;
    }

}
