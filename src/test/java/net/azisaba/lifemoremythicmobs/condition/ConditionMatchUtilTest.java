package net.azisaba.lifemoremythicmobs.condition;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionMatchUtilTest {

    @Test
    void parseCommaSeparatedTrimsDeduplicatesAndSkipsEmpty() {
        assertEquals(List.of(), ConditionMatchUtil.parseCommaSeparated(null));
        assertEquals(List.of(), ConditionMatchUtil.parseCommaSeparated("   "));
        assertEquals(List.of(), ConditionMatchUtil.parseCommaSeparated(" , , "));
        assertEquals(List.of("a", "b"), ConditionMatchUtil.parseCommaSeparated("a, b, a, "));
    }

    @Test
    void anyStartsWithSupportsCommaSeparatedOrAndIgnoreCase() {
        List<String> prefixes = ConditionMatchUtil.parseCommaSeparated("cl6_, cl7_");
        assertTrue(ConditionMatchUtil.anyStartsWith("cl6_sword", prefixes, false));
        assertTrue(ConditionMatchUtil.anyStartsWith("cl7_bow", prefixes, false));
        assertFalse(ConditionMatchUtil.anyStartsWith("CL6_sword", prefixes, false));
        assertTrue(ConditionMatchUtil.anyStartsWith("CL6_sword", prefixes, true));
        assertFalse(ConditionMatchUtil.anyStartsWith("cl8_staff", prefixes, true));
        assertFalse(ConditionMatchUtil.anyStartsWith("cl6_sword", List.of(), true));
        assertFalse(ConditionMatchUtil.anyStartsWith("", prefixes, true));
    }

    @Test
    void anyContainsSupportsCommaSeparatedOrAndIgnoreCase() {
        List<String> substrings = ConditionMatchUtil.parseCommaSeparated("7_fa, 8_ex");
        assertTrue(ConditionMatchUtil.anyContains("cl7_fa5_wand", substrings, false));
        assertTrue(ConditionMatchUtil.anyContains("cl8_ex1_axe", substrings, false));
        assertFalse(ConditionMatchUtil.anyContains("CL7_FA5_wand", substrings, false));
        assertTrue(ConditionMatchUtil.anyContains("CL7_FA5_wand", substrings, true));
        assertFalse(ConditionMatchUtil.anyContains("cl6_normal", substrings, true));
    }

    @Test
    void anyEqualsSupportsExactAndIgnoreCase() {
        List<String> servers = ConditionMatchUtil.parseCommaSeparated("life1, life2");
        assertTrue(ConditionMatchUtil.anyEquals("life1", servers, false));
        assertTrue(ConditionMatchUtil.anyEquals("life2", servers, false));
        assertFalse(ConditionMatchUtil.anyEquals("LIFE1", servers, false));
        assertTrue(ConditionMatchUtil.anyEquals("LIFE1", servers, true));
        assertFalse(ConditionMatchUtil.anyEquals("life3", servers, true));
        assertFalse(ConditionMatchUtil.anyEquals(null, servers, true));
    }

    @Test
    void isSpearMaterialNameMatchesVanillaSpearsAndOptionalTrident() {
        assertTrue(ConditionMatchUtil.isSpearMaterialName("DIAMOND_SPEAR", false));
        assertTrue(ConditionMatchUtil.isSpearMaterialName("COPPER_SPEAR", false));
        assertTrue(ConditionMatchUtil.isSpearMaterialName("NETHERITE_SPEAR", false));
        assertFalse(ConditionMatchUtil.isSpearMaterialName("DIAMOND_SWORD", false));
        assertFalse(ConditionMatchUtil.isSpearMaterialName("TRIDENT", false));
        assertTrue(ConditionMatchUtil.isSpearMaterialName("TRIDENT", true));
        assertFalse(ConditionMatchUtil.isSpearMaterialName(null, true));
    }
}
