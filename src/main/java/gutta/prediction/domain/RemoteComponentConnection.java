package gutta.prediction.domain;

import gutta.prediction.util.EqualityUtil;

import static java.util.Objects.*;

/**
 * This class represents a remote component connection, i.e., a connection that has overhead and cannot share transactions between source and target. Traversal
 * of a remote connection must change the location in an event trace.
 */
public final class RemoteComponentConnection extends ComponentConnection {

    private final long overhead;

    private final TransactionPropagation transactionPropagation;

    /**
     * Creates a new connection from the given data.
     * 
     * @param source                 The source component of the connection
     * @param target                 The target component of the connection
     * @param overhead               The amount of overhead caused by this connection in an arbitrary, but clearly defined unit
     * @param transactionPropagation The transaction propagation that this connection supports
     * @param modified               Flag denoting whether this connection was modified by a scenario
     */
    public RemoteComponentConnection(Component source, Component target, long overhead, TransactionPropagation transactionPropagation, boolean modified) {
        super(source, target, modified);

        this.overhead = overhead;
        this.transactionPropagation = requireNonNull(transactionPropagation);
    }

    @Override
    public long overhead() {
        return this.overhead;
    }

    @Override
    public boolean isRemote() {
        return true;
    }

    @Override
    public TransactionPropagation transactionPropagation() {
        return this.transactionPropagation;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + (int) this.overhead;
    }

    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }

    private boolean equalsInternal(RemoteComponentConnection that) {
        if (!super.equalsInternal(that)) {
            return false;
        }

        return (this.overhead == that.overhead) && (this.transactionPropagation == that.transactionPropagation);
    }

    @Override
    public String toString() {
        return "remote " + this.source() + " -> " + this.target();
    }

}
