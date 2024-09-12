package gutta.prediction.event;

public record ProcessLocation(String hostname, int processId) implements Location {
    
    @Override
    public boolean isArtificial() {
        return false;
    }

}
