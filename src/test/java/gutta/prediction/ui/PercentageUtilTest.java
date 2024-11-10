package gutta.prediction.ui;

import org.junit.jupiter.api.Test;

import static gutta.prediction.ui.PercentageUtil.calculateChangePercentage;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for the class {@link PercentageUtil}.
 */
class PercentageUtilTest {
    
    /**
     * Test case: The change percentage relative to zero is NaN.
     */
    @Test
    void percentageComparedToZero() {
        assertEquals(Double.NaN, calculateChangePercentage(0.0, 1.0));
    }
    
    /**
     * Test case: The change percentage of no change is zero.
     */
    @Test
    void percentageOfNoChange() {
        assertEquals(0.0, calculateChangePercentage(1.0, 1.0));
    }
    
    /**
     * Test case: The change percentage of an increase is larger than zero.
     */
    @Test
    void percentageOfPositiveChange() {
        assertEquals(0.5, calculateChangePercentage(1.0, 1.5));
    }
    
    /**
     * Test case: The change percentage of a decrease is less than zero.
     */
    @Test
    void percentageOfNegativeChange() {
        assertEquals(-0.5, calculateChangePercentage(2.0, 1.0));
    }
    
}
