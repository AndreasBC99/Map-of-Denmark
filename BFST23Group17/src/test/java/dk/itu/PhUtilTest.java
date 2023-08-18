package dk.itu;

import dk.itu.datastructure.phtree.PhUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PhUtilTest {
    private PhUtils phUtils;

    @BeforeEach
    public void setUp() {
        phUtils = new PhUtils();
    }

    @Test
    public void testSmallestPossibleFloat() {
        boolean[] input = {false, true, false, false, false, false, false, true, false, true, true, false};
        phUtils.smallestPossibleFloat(input);
        System.out.println();
    }

    @Test
    public void testBiggestPossibleFloat() {
        boolean[] input = {false, true, false, false, false, false, false, true, false, true, true, false};
        phUtils.biggestPossibleFloat(input);
        System.out.println();
    }

    @Test
    public void testCompareTo() {
        boolean[] inputA = {false, true, false, false, false, false, false, true,
                false, true, true, true, true, false, false, false, false, true,
                true, true, false, false, false, false, false, true, true, false,
                true, true, false, false};
        boolean[] inputB = {false, true, false, false, false, false, false, true,
                false, true, true, true, true, true, true, true, true, true, true,
                true, true, true, true, true, true, true, true, true, true,
                true, true, true};

        int actualValue = phUtils.compareFloats(inputA, inputB);
        assertEquals(-1, actualValue);

    }

    @Test
    public void testIntToBits() {
        int i = 5;
        boolean[] actualValue = phUtils.intToBits(5);
        boolean[] expectedValue = {false, true, false, true};
        assertArrayEquals(expectedValue, actualValue);

    }




}
