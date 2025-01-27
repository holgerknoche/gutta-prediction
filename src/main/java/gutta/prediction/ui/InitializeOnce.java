package gutta.prediction.ui;

import java.util.function.Supplier;

/**
 * Utility class to implement (non-thread-safe) once-only initialization of an object.
 * 
 * @param <T> The type of the object to initialize
 */
class InitializeOnce<T> {
    
    private Supplier<T> valueSupplier;
    
    private boolean initialized;
    
    private T value;
    
    public InitializeOnce(Supplier<T> valueSupplier) {
        this.valueSupplier = valueSupplier;
        this.initialized = false;
    }
    
    public T get() {
        if (!this.initialized) {
            this.value = this.valueSupplier.get();
            
            this.valueSupplier = null;
            this.initialized = true;            
        }
        
        return this.value;
    }

}
