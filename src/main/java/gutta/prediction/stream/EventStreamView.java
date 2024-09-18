package gutta.prediction.stream;

import gutta.prediction.event.MonitoringEvent;

public interface EventStreamView {
    
    MonitoringEvent lookahead(int amount);
    
    MonitoringEvent lookback(int amount);

}
