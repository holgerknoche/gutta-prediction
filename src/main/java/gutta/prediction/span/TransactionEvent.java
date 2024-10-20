package gutta.prediction.span;

import gutta.prediction.util.EqualityUtil;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class TransactionEvent extends SpanEvent {

    private final TransactionEventType type;
    
    private final String message;
    
    public TransactionEvent(long timestamp, TransactionEventType type) {
        this(timestamp, type, "");
    }
    
    public TransactionEvent(long timestamp, TransactionEventType type, String message) {
        super(timestamp);
        
        this.type = requireNonNull(type);
        this.message = requireNonNull(message);
    }

    public TransactionEventType type() {
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
    
    @Override
    public int hashCode() {
        return (int) this.timestamp() + this.type.hashCode();
    } 
    
    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    private boolean equalsInternal(TransactionEvent that) {
        if (!super.equalsInternal(that)) {
            return false;
        }
        
        return (this.type() == that.type()) &&
                Objects.equals(this.message(), that.message());
    }
    
    @Override
    public String toString() {
        return "Transaction Event '" + this.type() + "' at " + this.timestamp();
    }
    
    public enum TransactionEventType {
        START,
        COMMIT,
        IMPLICIT_ABORT,
        EXPLICIT_ABORT
    }
    
}
