package net.azisaba.lifemoremythicmobs.listener;

import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.core.mobs.ActiveMob;

/**
 * Per-mob switch for the MythicMobs 4.12 damage behaviour restored by {@link LegacySelfDamageListener}
 * and {@link LegacyMobDamageListener}. Mobs follow 5.12 unless their config opts in:
 * <pre>
 * LegacyDamage: true          # every part
 * LegacyDamage:               # or part by part; parts not listed are on
 *   EnvironmentTrigger: false
 * </pre>
 */
final class LegacyDamage {
    private static final String KEY = "LegacyDamage";

    enum Part {
        SELF_DAMAGE("SelfDamage"),
        ENVIRONMENT_TRIGGER("EnvironmentTrigger"),
        CANCELLED_THREAT("CancelledThreat"),
        ZERO_MODIFIER("ZeroModifier"),
        MOB_ARMOR("MobArmor"),
        MODIFIER_ORDER("ModifierOrder");

        private final String key;

        Part(String key) {
            this.key = key;
        }
    }

    private LegacyDamage() {
    }

    static boolean enabled(ActiveMob mob, Part part) {
        MythicConfig config = mob.getType() == null ? null : mob.getType().getConfig();
        if (config == null) return false;
        if (config.isConfigurationSection(KEY)) {
            return config.getBoolean(KEY + "." + part.key, true);
        }
        return config.getBoolean(KEY, false);
    }
}
