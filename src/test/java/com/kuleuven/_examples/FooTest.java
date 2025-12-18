package com.kuleuven._examples;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FooTest {

    @Test
    void testNegativeLessThanMinusTenEven() {
        // x < 0, x < -10, even
        int result = Foo.foo(-12);
        assertEquals(-4, result);
    }

    @Test
    void testNegativeBetweenMinusTenAndZeroNotMinusFive() {
        // x < 0, -10 <= x < 0, x != -5
        int result = Foo.foo(-3);
        assertEquals(-1, result);
    }

    @Test
    void testNonNegativeBetweenZeroAndTen() {
        // x >= 0, 0 <= x <= 10, x + 3.14 < 256
        int result = Foo.foo(7);
        assertEquals(10, result);
    }

    @Test
    void testGreaterThanTenSinBelowThreshold() {
        // x > 10, sin(x) <= 0.5
        int result = Foo.foo(11); // sin(11) ≈ -0.999
        assertEquals(102, result);
    }
}
