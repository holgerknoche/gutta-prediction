package gutta.prediction.datageneration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Transition graph for the {@link RandomTraceGenerator}.
 * 
 * @param <T> The type of object associated with the vertices
 */
class TransitionGraph<T> {

    private final List<Vertex<T>> vertices;

    @SafeVarargs
    public TransitionGraph(Vertex<T>... vertices) {
        this(List.of(vertices));
    }

    public TransitionGraph(List<Vertex<T>> vertices) {
        this.vertices = vertices;
    }

    public void validate() {
        this.vertices.forEach(Vertex::validate);
    }

    public void stackWalk(Vertex<T> startVertex, int numberOfTransitions, int maxDepth, StackWalkListener<T> listener) {
        var walker = new StackWalker<>(listener, new Random());
        walker.walk(startVertex, numberOfTransitions, maxDepth);
    }

    public static class Vertex<T> {

        private final T label;

        private List<Edge<T>> edges;

        public Vertex(T label) {
            this.label = label;
            this.edges = new ArrayList<>();
        }

        Vertex<T> edge(double probability, Vertex<T> target) {
            this.edges.add(new Edge<>(probability, target));
            return this;
        }

        public T label() {
            return this.label;
        }
        
        void edges(List<Edge<T>> edges) {
            this.edges = edges;
        }

        public List<Edge<T>> edges() {
            return this.edges;
        }

        public void validate() {
            var sumOfProbabilities = 0.0;

            for (var edge : this.edges()) {
                sumOfProbabilities += edge.probability();
            }

            // Round the sum to five decimal places to avoid double precision issues
            var roundedSum = (int) (sumOfProbabilities * 100000.0);
            if (roundedSum != 100000 && roundedSum != 99999) {
                throw new IllegalStateException(
                        "Sum of transition probabilities is not equal to 1 (" + sumOfProbabilities + ") for vertex '" + this.label() + "'.");
            }
        }

        Edge<T> selectEdge(double probability) {
            var currentProbability = 0.0;

            for (var edge : this.edges()) {
                currentProbability += edge.probability();

                if (probability <= currentProbability) {
                    return edge;
                }
            }

            throw new IllegalStateException("Could not determine edge for probability " + probability + ".");
        }

        @Override
        public String toString() {
            return "Vertex " + this.label();
        }

    }

    public record Edge<T>(double probability, Vertex<T> target) {

    }

    public interface StackWalkListener<T> {

        void onVertexEntry(Vertex<T> vertex);

        void onVertexExit(Vertex<T> vertex);

    }

}
