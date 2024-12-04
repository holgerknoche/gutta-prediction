package gutta.prediction.simulation;

import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.util.EqualityUtil;

/**
 * A {@link TopLevelTransaction} represents a standalone transaction that can itself be committed or reverted. Top-level transactions can have subordinates,
 * which are committed or aborted depending on the outcome of the top-level transaction.
 */
class TopLevelTransaction extends Transaction {

    private final Demarcation demarcation;

    public TopLevelTransaction(String id, MonitoringEvent startEvent, Location location, Demarcation demarcation) {
        super(id, startEvent, location);

        this.demarcation = demarcation;
    }

    @Override
    public boolean isTopLevel() {
        return true;
    }

    @Override
    public Demarcation demarcation() {
        return this.demarcation;
    }

    @Override
    Outcome commit() {
        var successfullyPrepared = this.prepare();

        var outcome = (successfullyPrepared) ? Outcome.COMMITTED : Outcome.ABORTED;
        this.complete(outcome);

        return outcome;
    }

    @Override
    Outcome abort() {
        var outcome = Outcome.ABORTED;
        this.complete(outcome);

        return outcome;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }

    private boolean equalsInternal(TopLevelTransaction that) {
        return super.equalsInternal(that);
    }

    @Override
    public String toString() {
        return "Top level transaction " + this.id() + " at location " + this.location();
    }

}
