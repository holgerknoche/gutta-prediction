package gutta.prediction.domain;

public record ComponentAllocation<T>(T object, boolean modified, Component component) {

}
