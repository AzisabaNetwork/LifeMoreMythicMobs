package net.azisaba.lifemoremythicmobs.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumericComparisonTest {
    @Test
    void supportsStandardComparisonOperators() {
        assertTrue(NumericComparison.matches(5, "5"));
        assertTrue(NumericComparison.matches(5, "==5"));
        assertFalse(NumericComparison.matches(4, "5"));
        assertTrue(NumericComparison.matches(5, ">=5"));
        assertTrue(NumericComparison.matches(6, ">=5"));
        assertFalse(NumericComparison.matches(4, ">=5"));
        assertTrue(NumericComparison.matches(5, "<=5"));
        assertTrue(NumericComparison.matches(6, ">5"));
        assertFalse(NumericComparison.matches(5, ">5"));
        assertTrue(NumericComparison.matches(4, "<5"));
        assertTrue(NumericComparison.matches(4, "!=5"));
        assertFalse(NumericComparison.matches(5, "!=5"));
    }

    @Test
    void supportsRangesAndRejectsInvalidInput() {
        assertTrue(NumericComparison.matches(5, "3to7"));
        assertTrue(NumericComparison.matches(3, "3to7"));
        assertTrue(NumericComparison.matches(7, "3-7"));
        assertFalse(NumericComparison.matches(8, "3-7"));
        assertFalse(NumericComparison.matches(5, ""));
        assertFalse(NumericComparison.matches(5, "abc"));
    }
}
