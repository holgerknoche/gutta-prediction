package gutta.prediction.ui;

class PercentageUtil {
    
    public static double calculateChangePercentage(double oldValue, double newValue) {
        if (oldValue == 0.0) {
            return Double.NaN;
        } else {
            return (newValue / oldValue) - 1.0;
        }
    }

}
