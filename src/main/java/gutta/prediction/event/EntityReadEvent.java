package gutta.prediction.event;

public record EntityReadEvent(long traceId, long timestamp, Location location, String entityType, String entityIdentifier) implements MonitoringEvent {

    @Override
    public <R> R accept(MonitoringEventVisitor<R> visitor) {
        return visitor.handleEntityReadEvent(this);
    }

}
