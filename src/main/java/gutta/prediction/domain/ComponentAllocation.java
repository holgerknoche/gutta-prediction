package gutta.prediction.domain;

import gutta.prediction.util.EqualityUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * As the name implies, a {@link ComponentAllocation} allocates objects of a given type to a {@link Component}.
 * In addition to the allocation itself, this class tracks whether this allocation was changed due to a scenario.  
 * 
 * @param <T> The type of the objects that are allocated
 */
class ComponentAllocation<T> {
    
    private final Map<T, ComponentAllocationEntry<T>> allocation;
    
    public ComponentAllocation() {
        this.allocation = new HashMap<>();
    }
    
    public ComponentAllocation(ComponentAllocation<T> other) {
        this.allocation = new HashMap<>(other.allocation);
    }
    
    public Optional<ComponentAllocationEntry<T>> get(T value) {
        return Optional.ofNullable(this.allocation.get(value));
    }
    
    public Set<T> keySet() {
        return this.allocation.keySet();
    }
    
    public Collection<ComponentAllocationEntry<T>> values() {
        return this.allocation.values();
    }
    
    public void addAllocation(T element, ComponentAllocationEntry<T> allocationEntry) {
        this.allocation.put(element, allocationEntry);
    }
    
    public void replaceOrAddAllocation(T elementToReplace, T newElement, ComponentAllocationEntry<T> newAllocationEntry) {
        if (elementToReplace != null) {
            this.allocation.remove(elementToReplace);
        }
        
        this.allocation.put(newElement, newAllocationEntry);
    }
    
    @Override
    public int hashCode() {
        return this.allocation.hashCode();
    }
    
    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    private boolean equalsInternal(ComponentAllocation<T> that) {
        return this.allocation.equals(that.allocation);
    }

}
