package gutta.prediction.simulation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation documents the simulation mode required for a specific method of the {@link TraceSimulationListener} interface to be invoked.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
@Documented
@interface RequiredSimulationMode {

    /**
     * Returns the simulation mode for the annotated method to be invoked.
     * 
     * @return see above
     */
    TraceSimulationMode value();

}
