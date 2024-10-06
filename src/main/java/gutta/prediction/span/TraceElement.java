package gutta.prediction.span;

interface TraceElement {

    /**
     * Accepts the given visitor and invokes the appropriate operation for this element. 
     * 
     * @param <R> The result type of the visitor operation
     * @param visitor The visitor to accept
     * @return The result of the visitor operation on this element
     */
    public <R> R accept(TraceElementVisitor<R> visitor);
    
    /**
     * Traverses this object and its children and invokes the appropriate operation for each element (including this).
     * 
     * @param visitor The visitor to accept
     */
    public void traverse(TraceElementVisitor<?> visitor);

}
