package gutta.prediction.domain;

public record ComponentAllocation<T>(T object, boolean synthetic, Component component) {

}
