package gutta.prediction.span;

import static java.util.Objects.requireNonNull;

public class TransactionEvent extends SpanEvent {

    private final Type type;
    
    private final String message;
    
    public TransactionEvent(long timestamp, Type type) {
        this(timestamp, type, "");
    }
    
    public TransactionEvent(long timestamp, Type type, String message) {
        super(timestamp);
        
        this.type = requireNonNull(type);
        this.message = requireNonNull(message);
    }

    public Type type() {
        return this.type;
    }
    
    public String message() {
        return this.message;
    }
    
    @Override
    public <R> R accept(TraceElementVisitor<R> visitor) {
        return visitor.handleTransactionEvent(this);
    }

    @Override
    public void traverse(TraceElementVisitor<?> visitor) {
        this.accept(visitor);        
    }
    
    public enum Type {
        START,
        COMMIT,
        IMPLICIT_ABORT,
        EXPLICIT_ABORT
    }
    
}
