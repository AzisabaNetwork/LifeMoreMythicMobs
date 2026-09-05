package net.azisaba.lifemoremythicmobs.listener;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

final class LegacyDamageModifierScope implements AutoCloseable {
    private static final String SKILL = "SKILL";
    private static final ThreadLocal<Deque<LegacyDamageModifierScope>> ACTIVE =
            ThreadLocal.withInitial(ArrayDeque::new);

    private final Map<String, Double> modifiers;
    private final boolean previouslyPresent;
    private final Double previousValue;
    private boolean closed;

    private LegacyDamageModifierScope(Map<String, Double> modifiers) {
        this.modifiers = modifiers;
        this.previouslyPresent = modifiers.containsKey(SKILL);
        this.previousValue = modifiers.get(SKILL);
    }

    static LegacyDamageModifierScope enter(Map<String, Double> modifiers, boolean elemental) {
        LegacyDamageModifierScope scope = new LegacyDamageModifierScope(modifiers);
        Deque<LegacyDamageModifierScope> active = ACTIVE.get();

        if (elemental) {
            modifiers.remove(SKILL);
        } else if (!modifiers.containsKey(SKILL)) {
            Double inherited = findInheritedSkillModifier(active, modifiers);
            if (inherited != null) {
                modifiers.put(SKILL, inherited);
            }
        }

        active.push(scope);
        return scope;
    }

    private static Double findInheritedSkillModifier(
            Deque<LegacyDamageModifierScope> active,
            Map<String, Double> modifiers
    ) {
        for (LegacyDamageModifierScope scope : active) {
            if (scope.modifiers == modifiers && scope.previouslyPresent) {
                return scope.previousValue;
            }
        }
        return null;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;

        Deque<LegacyDamageModifierScope> active = ACTIVE.get();
        active.removeFirstOccurrence(this);
        if (active.isEmpty()) {
            ACTIVE.remove();
        }

        if (previouslyPresent) {
            modifiers.put(SKILL, previousValue);
        } else {
            modifiers.remove(SKILL);
        }
    }
}
