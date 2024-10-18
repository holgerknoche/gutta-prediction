package gutta.prediction.event;

public sealed interface UseCaseEvent extends MonitoringEvent permits UseCaseStartEvent, UseCaseEndEvent {
    
    String name();

}
