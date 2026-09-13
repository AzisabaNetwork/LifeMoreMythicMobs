package net.azisaba.lifemoremythicmobs.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ElementalDefenseMathTest {
    @Test
    void combinesQuarterVanillaArmorAndFullToughnessWithElementalDefense() {
        double actual = ElementalDefenseMath.reduce(100.0D, 20.0D, 8.0D, 15.0D, 4.0D, false, false);
        double expected = 100.0D / (20.0D * 0.25D * 0.025D + 15.0D * 0.025D + 1.0D)
                / ((8.0D + 4.0D) * 0.025D + 1.0D);
        assertEquals(expected, actual, 1.0E-12D);
    }

    @Test
    void ignoreArmorOnlyRemovesVanillaValues() {
        double actual = ElementalDefenseMath.reduce(100.0D, 20.0D, 8.0D, 15.0D, 4.0D, true, false);
        double expected = 100.0D / (15.0D * 0.025D + 1.0D) / (4.0D * 0.025D + 1.0D);
        assertEquals(expected, actual, 1.0E-12D);
    }

    @Test
    void ignoreElementOnlyRemovesElementalValues() {
        double actual = ElementalDefenseMath.reduce(100.0D, 20.0D, 8.0D, 15.0D, 4.0D, false, true);
        double expected = 100.0D / (20.0D * 0.25D * 0.025D + 1.0D) / (8.0D * 0.025D + 1.0D);
        assertEquals(expected, actual, 1.0E-12D);
    }

    @Test
    void bothIgnoreFlagsLeaveDamageUnchanged() {
        assertEquals(100.0D,
                ElementalDefenseMath.reduce(100.0D, 20.0D, 8.0D, 15.0D, 4.0D, true, true),
                1.0E-12D);
    }

    @Test
    void preparedAmountProducesTheRequestedResultAfterCombatMathRuns() {
        double prepared = ElementalDefenseMath.prepareForCombatMathPipeline(
                100.0D, 20.0D, 8.0D, 15.0D, 4.0D, false, false);
        double afterCombatMath = prepared / (20.0D * 0.025D + 1.0D) / (8.0D * 0.025D + 1.0D);
        double expected = ElementalDefenseMath.reduce(
                100.0D, 20.0D, 8.0D, 15.0D, 4.0D, false, false);
        assertEquals(expected, afterCombatMath, 1.0E-12D);
    }

    @Test
    void ignoreArmorNeedsNoPipelineCompensation() {
        double prepared = ElementalDefenseMath.prepareForCombatMathPipeline(
                100.0D, 20.0D, 8.0D, 15.0D, 4.0D, true, false);
        double expected = ElementalDefenseMath.reduce(
                100.0D, 20.0D, 8.0D, 15.0D, 4.0D, true, false);
        assertEquals(expected, prepared, 1.0E-12D);
    }
}
