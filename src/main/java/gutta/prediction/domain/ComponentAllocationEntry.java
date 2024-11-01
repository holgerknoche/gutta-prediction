package gutta.prediction.domain;

public record ComponentAllocationEntry<T>(T object, boolean modified, Component component) {

}
