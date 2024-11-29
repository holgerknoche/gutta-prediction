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

    /**
     * Performs a "stack" walk of this graph, i.e., traverses the graph to emulate random sequences of function calls.
     * 
     * @param startVertex The vertex to start at
     * @param numberOfTransitions The number of transitions to perform during the walk
     * @param maxDepth The maximum depth of the emulated call stack
     * @param listener A listener to perform actions based on the walk
     */
    public void stackWalk(Vertex<T> startVertex, int numberOfTransitions, int maxDepth, StackWalkListener<T> listener) {
        var walker = new StackWalker<>(listener, new Random());
        walker.walk(startVertex, numberOfTransitions, maxDepth);
    }

    /**
     * Vertex of a {@link TransitionGraph}.
     * 
     * @param <T> The type of the object associated to this vertex
     */
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

    /**
     * Edge from one vertex to another in a transition graph.
     * 
     * @param <T> The type of the objects associated with the vertices
     * @param probability The probability that this edge is traversed
     * @param target The target vertex of this edge
     */
    public record Edge<T>(double probability, Vertex<T> target) {

    }

    /**
     * Listener interface for a "stack" walk along a transition graph, i.e., vertices that have been entered are exited in reverse order, emulating a call
     * stack.
     * 
     * @param <T> The type of object associated with the vertices
     * @see StackWalker
     * @see TransitionGraph#stackWalk(Vertex, int, int, StackWalkListener)
     */
    public interface StackWalkListener<T> {

        /**
         * This method is invoked when a vertex is entered.
         * 
         * @param vertex The vertex that was entered
         */
        void onVertexEntry(Vertex<T> vertex);

        /**
         * This method is invoked when a vertex is exited.
         * 
         * @param vertex The vertex that was exited
         */
        void onVertexExit(Vertex<T> vertex);

    }

}
