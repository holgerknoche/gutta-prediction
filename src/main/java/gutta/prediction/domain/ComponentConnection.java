package gutta.prediction.domain;

import gutta.prediction.util.EqualityUtil;

import java.util.Objects;

import static java.util.Objects.*;

/**
 * A {@link ComponentConnection} represents a connection between two {@linkplain Component components} with the associated attributes.
 */
public abstract class ComponentConnection {

    private final Component source;

    private final Component target;

    private final boolean modified;

    /**
     * Creates a new connection from the given data.
     * 
     * @param source   The source component of the connection
     * @param target   The target component of the connection
     * @param modified Flag denoting whether this connection was modified by a scenario
     */
    protected ComponentConnection(Component source, Component target, boolean modified) {
        this.source = requireNonNull(source);
        this.target = requireNonNull(target);
        this.modified = modified;
    }

    /**
     * Returns the source component of this connection.
     * 
     * @return see above
     */
    public Component source() {
        return this.source;
    }

    /**
     * Returns the target component of this connection.
     * 
     * @return see above
     */
    public Component target() {
        return this.target;
    }

    /**
     * Denotes whether this connection was modified by a scenario.
     * 
     * @return see above
     */
    public boolean isModified() {
        return this.modified;
    }

    /**
     * Returns the amount of overhead caused by this connection. 
     * 
     * @return The amount of overhead in an arbitrary, but clearly defined unit
     */
    public abstract long overhead();

    /**
     * Denotes whether this connection is remote in the sense that it cannot share transactions.
     * 
     * @return see above
     */
    public abstract boolean isRemote();

    /**
     * Denotes whether this connection is able to propagate transactions in some way.
     * 
     * @return {@code True} if this connection can propagate transactions, {@code false} if it cannot propagate transactions at all
     */
    public boolean canPropagateTransactions() {
        return this.transactionPropagation().canPropagateTransactions();
    }

    /**
     * Returns the transaction propagation that this connection supports.
     * 
     * @return see above
     */
    public abstract TransactionPropagation transactionPropagation();

    @Override
    public int hashCode() {
        return Objects.hash(this.source, this.target);
    }

    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }

    /**
     * Checks the equality of this object to the given one by comparing the attributes.
     * 
     * @param that The object to compare to
     * @return {@code True} if the objects are equal, {@code false} otherwise
     */
    protected boolean equalsInternal(ComponentConnection that) {
        return (this.modified == that.modified) && //
                Objects.equals(this.source, that.source) && //
                Objects.equals(this.target, that.target);
    }

}
