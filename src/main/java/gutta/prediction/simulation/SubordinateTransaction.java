package gutta.prediction.simulation;

import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.util.EqualityUtil;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

class SubordinateTransaction extends Transaction {
    
    private final Transaction parent;

    public SubordinateTransaction(String id, MonitoringEvent startEvent, Location location, Transaction parent) {
        super(id, startEvent, location);
        
        this.parent = requireNonNull(parent);
        
        parent.registerSubordinate(this);
    }
    
    @Override
    public boolean isTopLevel() {
        return false;
    }
    
    @Override
    public Demarcation demarcation() {
        return Demarcation.IMPLICIT;
    }
    
    @Override
    Outcome commit() {
        throw new IllegalStateException("Attempt to commit subordinate transaction + '" + this.id() + "'.");
    }
    
    @Override
    Outcome abort() {
        throw new IllegalStateException("Attempt to commit subordinate transaction + '" + this.id() + "'.");
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hashCode(this.parent);
    }
    
    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    private boolean equalsInternal(SubordinateTransaction that) {
        if (!super.equalsInternal(that)) {
            return false;
        }
        
        // Only compare IDs to avoid cycles
        return Objects.equals(this.parent.id(), that.parent.id());
    }
    
    @Override
    public String toString() {
        return "Subordinate transaction " + this.id() + " at location " + this.location();
    }
    
}
