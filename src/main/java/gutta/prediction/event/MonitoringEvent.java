package gutta.prediction.event;

public interface MonitoringEvent {

    long traceId();

    long timestamp();

    Location location();

    default boolean isSynthetic() {
        return false;
    }
    
    <R> R accept(MonitoringEventVisitor<R> visitor);

}
