package dk.itu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {
    @Test
    public void test() {
        int[] x = new int[1];
        System.out.println(x.length);
        x[0] = 3;
        System.out.println(x.length);
        assertEquals(2, 2);
    }
}
