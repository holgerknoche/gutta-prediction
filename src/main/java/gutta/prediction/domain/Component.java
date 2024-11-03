package gutta.prediction.domain;

public record Component(String name) {
    
    public int hashCode() {
        return name.hashCode();
    }
    
}