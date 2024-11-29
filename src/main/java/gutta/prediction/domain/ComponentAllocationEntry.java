package gutta.prediction.domain;

import static java.util.Objects.*;

/**
 * A {@link ComponentAllocationEntry} represents the allocation of a particular object to a {@link Component} as part of a {@link ComponentAllocation}.
 * 
 * @param <T>       The type of the allocated object
 * @param object    The object to allocate
 * @param modified  Flag denoting whether this allocation was changed due to a scenario
 * @param component The component to allocate the object to
 */
public record ComponentAllocationEntry<T>(T object, boolean modified, Component component) {

    /**
     * Creates a new allocation entry with the given data.
     * 
     * @param object    The object to allocate
     * @param modified  Flag denoting whether this allocation was changed due to a scenario
     * @param component The component to allocate the object to
     */
    public ComponentAllocationEntry(T object, boolean modified, Component component) {
        this.object = requireNonNull(object);
        this.modified = modified;
        this.component = requireNonNull(component);
    }

}
