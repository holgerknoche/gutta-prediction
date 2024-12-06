package gutta.prediction.span;

import gutta.prediction.util.EqualityUtil;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * A {@link TransactionEvent} is a span event that is related to transaction boundaries, such as the commit of a transaction.
 */
public final class TransactionEvent extends SpanEvent {

    private final TransactionEventType type;

    private final String message;

    /**
     * Creates a new event of the given type at the given timestamp.
     * 
     * @param timestamp The timestamp at which the even occurred
     * @param type      The type of the event
     */
    public TransactionEvent(long timestamp, TransactionEventType type) {
        this(timestamp, type, "");
    }

    /**
     * Creates a new event of the given type at the given timestamp with the given message.
     * 
     * @param timestamp The timestamp at which the even occurred
     * @param type      The type of the event
     * @param message   The message associated with the event (e.g., the cause of an implicit abort)
     */
    public TransactionEvent(long timestamp, TransactionEventType type, String message) {
        super(timestamp);

        this.type = requireNonNull(type);
        this.message = requireNonNull(message);
    }

    /**
     * Returns the type of this event.
     * 
     * @return see above
     */
    public TransactionEventType type() {
        return this.type;
    }

    /**
     * Returns the message associated with this event.
     * 
     * @return see above
     */
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

        return (this.type() == that.type()) && //
                Objects.equals(this.message(), that.message());
    }

    @Override
    public String toString() {
        return "Transaction Event '" + this.type() + "' at " + this.timestamp();
    }

    /**
     * Enumeration of all possible transaction event types.
     */
    public enum TransactionEventType {

        /**
         * Event type for the start of a transaction.
         */
        START,

        /**
         * Event type for the commit of a transaction.
         */
        COMMIT,

        /**
         * Event type for the implicit abort of a transaction.
         */
        IMPLICIT_ABORT,

        /**
         * Event type for the explicit abort of a transaction.
         */
        EXPLICIT_ABORT
    }

}
