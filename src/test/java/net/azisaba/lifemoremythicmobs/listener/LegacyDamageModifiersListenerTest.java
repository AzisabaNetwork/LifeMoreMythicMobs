package net.azisaba.lifemoremythicmobs.listener;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LegacyDamageModifiersListenerTest {

    @Test
    void elementalDamageTemporarilyHidesSkillModifier() {
        Map<String, Double> modifiers = modifiers();

        try (LegacyDamageModifierScope ignored =
                     LegacyDamageModifierScope.enter(modifiers, true)) {
            assertFalse(modifiers.containsKey("SKILL"));
            assertEquals(0.7, modifiers.get("FIRE"));
        }

        assertEquals(0.0, modifiers.get("SKILL"));
    }

    @Test
    void untypedDamageKeepsSkillModifier() {
        Map<String, Double> modifiers = modifiers();

        try (LegacyDamageModifierScope ignored =
                     LegacyDamageModifierScope.enter(modifiers, false)) {
            assertEquals(0.0, modifiers.get("SKILL"));
        }

        assertEquals(0.0, modifiers.get("SKILL"));
    }

    @Test
    void nestedUntypedDamageRestoresSkillOnlyForItsOwnScope() {
        Map<String, Double> modifiers = modifiers();

        try (LegacyDamageModifierScope elemental =
                     LegacyDamageModifierScope.enter(modifiers, true)) {
            assertFalse(modifiers.containsKey("SKILL"));

            try (LegacyDamageModifierScope untyped =
                         LegacyDamageModifierScope.enter(modifiers, false)) {
                assertEquals(0.0, modifiers.get("SKILL"));
            }

            assertFalse(modifiers.containsKey("SKILL"));
        }

        assertEquals(0.0, modifiers.get("SKILL"));
    }

    @Test
    void nestedElementalDamagePreservesOuterScope() {
        Map<String, Double> modifiers = modifiers();

        try (LegacyDamageModifierScope outer =
                     LegacyDamageModifierScope.enter(modifiers, true)) {
            try (LegacyDamageModifierScope inner =
                         LegacyDamageModifierScope.enter(modifiers, true)) {
                assertFalse(modifiers.containsKey("SKILL"));
            }
            assertFalse(modifiers.containsKey("SKILL"));
        }

        assertEquals(0.0, modifiers.get("SKILL"));
    }

    private static Map<String, Double> modifiers() {
        Map<String, Double> modifiers = new HashMap<>();
        modifiers.put("SKILL", 0.0);
        modifiers.put("ENTITY_ATTACK", 0.0);
        modifiers.put("FIRE", 0.7);
        return modifiers;
    }
}
