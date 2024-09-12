package gutta.prediction.event;

public record EntityWriteEvent(long traceId, long timestamp, Location location, String entityType, String entityIdentifier) implements MonitoringEvent {

    @Override
    public <R> R accept(MonitoringEventVisitor<R> visitor) {
        return visitor.handleEntityWriteEvent(this);
    }

}
