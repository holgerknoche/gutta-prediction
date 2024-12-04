package gutta.prediction.simulation;

import gutta.prediction.event.ImplicitTransactionAbortEvent;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.util.EqualityUtil;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * This class represents a (ACID) transaction within the simulation. Transactions can be committed or aborted, and keep track of the changed entities. This
 * class furthermore emulates the two-phase commit protocol for subordinate transactions.
 */
public abstract class Transaction {

    private final String id;

    private final MonitoringEvent startEvent;

    private final Location location;

    private final Set<SubordinateTransaction> subordinates;

    private Outcome outcome;

    private boolean abortOnly;

    /**
     * Creates a new transaction from the given data.
     * 
     * @param id         The id of the transaction
     * @param startEvent The event that started this transaction
     * @param location   The location at which this transaction is executed
     */
    protected Transaction(String id, MonitoringEvent startEvent, Location location) {
        this.id = requireNonNull(id);
        this.startEvent = requireNonNull(startEvent);
        this.location = requireNonNull(location);

        this.subordinates = new HashSet<>();
        this.outcome = null;
        this.abortOnly = false;
    }

    /**
     * Returns the id of this transaction.
     * 
     * @return see above
     */
    public String id() {
        return this.id;
    }

    /**
     * Returns the event that started this transaction.
     * 
     * @return see above
     */
    public MonitoringEvent startEvent() {
        return this.startEvent;
    }

    /**
     * Returns the location at which this transaction is executed.
     * 
     * @return see above
     */
    public Location location() {
        return this.location;
    }

    /**
     * Returns the type of demarcation used for this transaction.
     * 
     * @return see above
     */
    public abstract Demarcation demarcation();

    abstract Outcome commit();

    void setAbortOnly() {
        this.abortOnly = true;
    }

    void registerImplicitAbort(ImplicitTransactionAbortEvent causingEvent) {
        this.setAbortOnly();
    }

    abstract Outcome abort();

    boolean prepare() {
        if (this.abortOnly) {
            return false;
        }

        boolean successful = true;
        for (var subordinate : this.subordinates) {
            var subordinateSuccessful = subordinate.prepare();
            successful = successful && subordinateSuccessful;
        }

        return successful;
    }

    void complete(Outcome outcome) {
        if (this.outcome != null && this.outcome != outcome) {
            throw new IllegalStateException("Attempt to change outcome of transaction '" + this.id() + "' from " + this.outcome + " to " + outcome + ".");
        }

        this.outcome = outcome;
        this.subordinates.forEach(subordinate -> subordinate.complete(outcome));
    }

    /**
     * Registers the given subordinate transaction.
     * 
     * @param subordinate The subordinate to register
     */
    protected void registerSubordinate(SubordinateTransaction subordinate) {
        this.subordinates.add(subordinate);
    }

    /**
     * Performs the given action for this transaction and all its subordinates.
     * 
     * @param action The action to perform
     */
    public void forEach(Consumer<Transaction> action) {
        action.accept(this);
        this.subordinates.forEach(subordinate -> subordinate.forEach(action));
    }

    /**
     * Denotes whether this is a top-level transaction.
     * 
     * @return see above
     */
    public abstract boolean isTopLevel();

    /**
     * Denotes whether this is a subordinate transaction.
     * 
     * @return see above
     */
    public boolean isSubordinate() {
        return !this.isTopLevel();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.startEvent, this.location);
    }

    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }

    /**
     * Compares this transaction to the given one. This method is intended to be called only by {@link #equals(Object)} and specializations.
     * 
     * @param that The transaction to compare to
     * @return {@code True} if the two elements are equal
     */
    protected boolean equalsInternal(Transaction that) {
        return Objects.equals(this.id, that.id) && // 
                Objects.equals(this.startEvent, that.startEvent) && //
                Objects.equals(this.location, that.location) && //
                Objects.equals(this.subordinates, that.subordinates);
    }

    /**
     * Enumeration of all possible outcomes of a transaction.
     */
    enum Outcome {
        
        /**
         * Denotes that the transaction was committed.
         */
        COMMITTED,
        
        /**
         * Denotes that the transaction was aborted.
         */
        ABORTED
    }

    /**
     * Enumeration of all possible types of transaction demarcation.
     */
    public enum Demarcation {
        
        /**
         * Denotes that the transaction is explicitly demarcated, i.e., by issuing explicit commit and rollback commands.
         */
        EXPLICIT,
        
        /**
         * Denotes that the transaction is implicitly demarcated, i.e., managed by a container based on declarative annotations.
         */
        IMPLICIT
    }

}
