package gutta.prediction.datageneration;

import gutta.prediction.datageneration.TransitionGraph.StackWalkListener;
import gutta.prediction.datageneration.TransitionGraph.Vertex;

import java.util.ArrayDeque;
import java.util.Random;

class StackWalker<T> {

    private final StackWalkListener<T> listener;
    
    private final Random random;
            
    public StackWalker(StackWalkListener<T> listener, Random random) {
        this.listener = listener;
        this.random = random;
    }
    
    public void walk(Vertex<T> startVertex, int numberOfTransitions, int maxDepth) {
        var transitionCount = 0;
        var currentVertex = startVertex;
        var stack = new ArrayDeque<Vertex<T>>(maxDepth);
        
        this.listener.onVertexEntry(currentVertex);
        
        // Generate the desired number of transitions
        while (transitionCount < numberOfTransitions) {
           if (stack.size() < maxDepth) {
               // Add a new invocation if there is stack depth to spare
               var probability = this.random.nextDouble(1.0);
               var edge = currentVertex.selectEdge(probability);
               
               stack.push(currentVertex);
               
               currentVertex = edge.target();
               transitionCount++;
                                  
               this.listener.onVertexEntry(currentVertex);
           } else {
               // Otherwise, pop a random number of vertices of the stack before proceeding
               var numberOfPops = this.random.nextInt(stack.size());
               
               for (int popCount = 0; popCount < numberOfPops; popCount++) {
                   var poppedVertex = stack.pop();
                   this.listener.onVertexExit(poppedVertex);
               }
               
               currentVertex = stack.peek();
           }
        }
        
        // Leave the remaining vertices on the stack
        while (!stack.isEmpty()) {
            var poppedVertex = stack.pop();
            this.listener.onVertexExit(poppedVertex);
        }
    }
    
}
