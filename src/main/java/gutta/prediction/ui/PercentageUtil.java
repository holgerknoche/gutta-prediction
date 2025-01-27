package gutta.prediction.ui;

/**
 * Utility methods for handling percentages.
 */
class PercentageUtil {

    /**
     * Calculates the percentage of change from the one value to the other.
     * 
     * @param oldValue The value before the change
     * @param newValue The value after the change
     * @return The change percentage
     */
    public static double calculateChangePercentage(double oldValue, double newValue) {
        if (oldValue == 0.0) {
            return Double.NaN;
        } else {
            return (newValue / oldValue) - 1.0;
        }
    }

}
