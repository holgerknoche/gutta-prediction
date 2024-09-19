package gutta.prediction.simulation;

import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;

class SubordinateTransaction extends Transaction {

    public SubordinateTransaction(String id, MonitoringEvent startEvent, Location location, Transaction parent) {
        super(id, startEvent, location);
        
        parent.registerSubordinate(this);
    }
    
    @Override
    public boolean isSubordinate() {
        return true;
    }
    
}
