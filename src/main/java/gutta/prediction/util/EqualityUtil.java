package gutta.prediction.util;

import java.util.function.Predicate;

/**
 * Utility methods to facilitate the implementation of {@code equals}.
 */
public class EqualityUtil {

    /**
     * Provides the canonical {@code equals} implementation, including appropriate treatment for identical objects as well as handling objects of different
     * types. This implementation assumes that types are identical (i.e., no use of {@code instanceof}) for a symmetric implementation. It is intended to be
     * used as follows:
     * 
     * <pre>
     * class SomeType extends SuperType {
     * 
     *   public boolean equals(Object that) {
     *     return EqualityUtil.equals(this, that, this::equalsInternal);
     *   }
     * 
     *   protected boolean equalsInternal(SomeType that) {
     *     if (!super.equalsInternal(that)) {
     *       return false;
     *     }
     *     
     *     // Compare the necessary fields
     *     ...
     *   }
     * }
     * </pre>
     * 
     * The {@code equalsInternal} method can be {@code private} for leaf classes.
     * 
     * @param <T>               The type of the objects to compare
     * @param thisObject        The object to be considered {@code this}
     * @param thatObject        The object to compare the object to (may be {@code null})
     * @param equalityPredicate The predicate to check for equality (see example)
     * @return {@code True} if the objects are equal, {@code false} otherwise
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean equals(T thisObject, Object thatObject, Predicate<T> equalityPredicate) {
        if (thisObject == thatObject) {
            return true;
        } else if (thatObject != null && thisObject.getClass() == thatObject.getClass()) {
            return equalityPredicate.test((T) thatObject);
        } else {
            return false;
        }
    }

    private EqualityUtil() {
        // Private constructor, as we only have static methods
    }

}
