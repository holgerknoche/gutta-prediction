package gutta.prediction.simulation;

import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.util.EqualityUtil;

class TopLevelTransaction extends Transaction {

    public TopLevelTransaction(String id, MonitoringEvent startEvent, Location location) {
        super(id, startEvent, location);
    }                
    
    @Override
    public boolean isSubordinate() {
        return false;
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
