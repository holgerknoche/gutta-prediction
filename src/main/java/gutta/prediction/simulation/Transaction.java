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

public abstract class Transaction {

    private final String id;
    
    private final MonitoringEvent startEvent;
    
    private final Location location;
                
    private final Set<SubordinateTransaction> subordinates;
            
    private Outcome outcome;
    
    private boolean abortOnly;
    
    protected Transaction(String id, MonitoringEvent startEvent, Location location) {
        this.id = requireNonNull(id);
        this.startEvent = requireNonNull(startEvent);
        this.location = requireNonNull(location);
        
        this.subordinates = new HashSet<>();
        this.outcome = null;
        this.abortOnly = false;
    }
    
    public String id() {
        return this.id;
    }
    
    public MonitoringEvent startEvent() {
        return this.startEvent;
    }
    
    public Location location() {
        return this.location;
    }
    
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
            
    protected void registerSubordinate(SubordinateTransaction subordinate) {
        this.subordinates.add(subordinate);
    }
    
    public void forEach(Consumer<Transaction> action) {
        action.accept(this);
        this.subordinates.forEach(subordinate -> subordinate.forEach(action));
    }
        
    public abstract boolean isTopLevel();
    
    public boolean isSubordinate() {
        return !this.isTopLevel();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.startEvent, this.location);
    }
    
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    protected boolean equalsInternal(Transaction that) {
        return Objects.equals(this.id, that.id) &&
               Objects.equals(this.startEvent, that.startEvent) &&
               Objects.equals(this.location, that.location) &&
               Objects.equals(this.subordinates, that.subordinates);
    }        
    
    public enum Outcome {
        COMMITTED,
        ABORTED
    }
    
    public enum Demarcation {
        EXPLICIT,
        IMPLICIT
    }
        
}
