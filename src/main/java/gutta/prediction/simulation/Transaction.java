package gutta.prediction.simulation;

import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.util.EqualityUtil;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public abstract class Transaction {

    private final String id;
    
    private final MonitoringEvent startEvent;
    
    private final Location location;
            
    private final Set<SubordinateTransaction> subordinates;
    
    protected Transaction(String id, MonitoringEvent startEvent, Location location) {
        this.id = id;
        this.startEvent = startEvent;
        this.location = location;
        this.subordinates = new HashSet<>();
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
    
    protected void registerSubordinate(SubordinateTransaction subordinate) {
        this.subordinates.add(subordinate);
    }
    
    public abstract boolean isSubordinate();
            
    public Set<Transaction> getThisAndAllSubordinates() {
        var allSubordinates = new HashSet<Transaction>();
        
        this.collectSubordinates(allSubordinates::add);
        
        return allSubordinates;
    }
    
    private void collectSubordinates(Consumer<Transaction> collector) {
        collector.accept(this);
        
        for (Transaction subordinate : this.subordinates) {
            subordinate.collectSubordinates(collector);
        }
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
    
}
