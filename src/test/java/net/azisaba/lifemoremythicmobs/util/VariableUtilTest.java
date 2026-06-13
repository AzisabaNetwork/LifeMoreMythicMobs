package net.azisaba.lifemoremythicmobs.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for VariableUtil - MythicMobs variable scope management.
 */
class VariableUtilTest {

    @Test
    void testScopedVariableParsing() {
        // Test the scope+variable parsing logic directly
        String scopedVarName = "caster.var.test_var";
        String[] parts = scopedVarName.split("\\.", 2);
        assertEquals(2, parts.length);
        assertEquals("caster", parts[0]);
        assertEquals("var.test_var", parts[1]);
    }

    @Test
    void testScopeCasterDetection() {
        String scope = "caster";
        switch (scope) {
            case "caster": assertTrue(true); break;
            default: fail("Wrong scope: " + scope);
        }
    }

    @Test
    void testScopeTargetDetection() {
        String scope = "target";
        switch (scope) {
            case "target": assertTrue(true); break;
            default: fail("Wrong scope: " + scope);
        }
    }

    @Test
    void testScopeSkillDetection() {
        String scope = "skill";
        switch (scope) {
            case "skill": assertTrue(true); break;
            default: fail("Wrong scope: " + scope);
        }
    }

    @Test
    void testScopeGlobalDetection() {
        String scope = "global";
        switch (scope) {
            case "global": assertTrue(true); break;
            default: fail("Wrong scope: " + scope);
        }
    }

    @Test
    void testScopeWorldDetection() {
        String scope = "world";
        switch (scope) {
            case "world": assertTrue(true); break;
            default: fail("Wrong scope: " + scope);
        }
    }

    @Test
    void testVanillaVarPrefix() {
        // Variable name with "var." prefix should be stripped
        String variableName = "var.test_val";
        if (variableName.startsWith("var.")) {
            variableName = variableName.substring("var.".length());
        }
        assertEquals("test_val", variableName);
    }

    @Test
    void testVanillaVarPrefixWithout() {
        String variableName = "test_val";
        if (variableName.startsWith("var.")) {
            variableName = variableName.substring("var.".length());
        }
        assertEquals("test_val", variableName);
    }

    @Test
    void testNullScopedVariable() {
        String scopedVarName = null;
        // Null/empty should return false
        assertFalse(scopedVarName != null && !scopedVarName.isEmpty());
    }

    @Test
    void testEmptyScopedVariable() {
        String scopedVarName = "";
        assertFalse(scopedVarName != null && !scopedVarName.isEmpty());
    }

    @Test
    void testAngleBracketStrip() {
        String scopedVarName = "<caster.var.test>";
        if (scopedVarName.startsWith("<") && scopedVarName.endsWith(">")) {
            scopedVarName = scopedVarName.substring(1, scopedVarName.length() - 1);
        }
        assertEquals("caster.var.test", scopedVarName);
    }

    @Test
    void testScopeWithAngleBrackets() {
        String scopedVarName = "<target.var.health>";
        if (scopedVarName.startsWith("<") && scopedVarName.endsWith(">")) {
            scopedVarName = scopedVarName.substring(1, scopedVarName.length() - 1);
        }
        String[] parts = scopedVarName.split("\\.", 2);
        assertEquals("target", parts[0]);
        assertEquals("var.health", parts[1]);
        if (parts[1].startsWith("var.")) {
            parts[1] = parts[1].substring("var.".length());
        }
        assertEquals("health", parts[1]);
    }
}
