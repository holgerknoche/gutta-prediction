package gutta.prediction.domain;

/**
 * A {@link Component} is the central element of our approach, and groups other elements such as {@linkplain UseCase use cases} or {@linkplain ServiceCandidate
 * service candidates} into a coherent unit.
 * Furthermore, components can have {@linkplain ComponentConnection connections} to other components.
 * Depending on the nature of its connections, one or more components represent a microservice to be separated.
 */
public record Component(String name) {

    @Override
    public int hashCode() {
        // A custom hash code implementation proved to be much faster than the default implementation
        return name.hashCode();
    }

}