package gutta.prediction.domain;

import gutta.prediction.util.EqualityUtil;

/**
 * This class represents a local component connection, i.e., a connection that has no overhead and shares transactions between source and target.
 * Traversal of a local connection must not change the location in an event trace. 
 */
public final class LocalComponentConnection extends ComponentConnection {

    /**
     * Creates a new local connection from the given data.
     * 
     * @param source   The source component of the connection
     * @param target   The target component of the connection
     * @param modified Flag denoting whether this connection was modified by a scenario
     */
    public LocalComponentConnection(Component source, Component target, boolean modified) {
        super(source, target, modified);
    }

    @Override
    public long overhead() {
        return 0;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public TransactionPropagation transactionPropagation() {
        return TransactionPropagation.IDENTICAL;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }

    private boolean equalsInternal(LocalComponentConnection that) {
        return super.equalsInternal(that);
    }

    @Override
    public String toString() {
        return "local " + this.source() + " -- " + this.target();
    }

}
