package net.azisaba.lifemoremythicmobs.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SkillCheckManagerTest {
    @Test
    void evaluatesGreatGoodAndFailZones() {
        assertEquals(SkillCheckMath.CheckResult.GREAT,
                SkillCheckMath.evaluateZone(0.52, 0.50, 0.08, 0.20));
        assertEquals(SkillCheckMath.CheckResult.GOOD,
                SkillCheckMath.evaluateZone(0.65, 0.50, 0.08, 0.20));
        assertEquals(SkillCheckMath.CheckResult.FAIL,
                SkillCheckMath.evaluateZone(0.80, 0.50, 0.08, 0.20));
    }

    @Test
    void renderBarContainsCursorAndBrackets() {
        String bar = SkillCheckMath.renderBar("&e採掘", 0.5, 0.5, 0.08, 0.20, 20);
        assertTrue(bar.contains("◆"));
        assertTrue(bar.contains("["));
        assertTrue(bar.contains("]"));
        assertTrue(bar.contains("&6="));
    }
}
