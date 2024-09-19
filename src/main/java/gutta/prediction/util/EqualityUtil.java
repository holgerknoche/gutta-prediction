package gutta.prediction.util;

import java.util.function.Predicate;

public class EqualityUtil {
    
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
