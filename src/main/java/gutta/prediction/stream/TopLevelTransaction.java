package gutta.prediction.stream;

import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;

class TopLevelTransaction extends Transaction {

    public TopLevelTransaction(String id, MonitoringEvent startEvent, Location location) {
        super(id, startEvent, location);
    }                
    
    @Override
    public boolean isSubordinate() {
        return false;
    }
    
}
