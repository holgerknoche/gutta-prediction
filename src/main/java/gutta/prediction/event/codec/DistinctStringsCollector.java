package gutta.prediction.event.codec;

import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.MonitoringEventVisitor;

import java.util.HashMap;
import java.util.Map;

class DistinctStringsCollector implements MonitoringEventVisitor<Void> {
    
    private final Map<String, Integer> stringToIndex = new HashMap<>();
    
    private int currentIndex = 0;
    
    @SuppressWarnings("boxing")
    private void registerString(String value) {
        if (!this.stringToIndex.containsKey(value)) {
            this.stringToIndex.put(value, this.currentIndex++);
        }
    }
    
    @Override
    public Void handleEntityReadEvent(EntityReadEvent event) {
        // TODO Auto-generated method stub
        return MonitoringEventVisitor.super.handleEntityReadEvent(event);
    }

}
