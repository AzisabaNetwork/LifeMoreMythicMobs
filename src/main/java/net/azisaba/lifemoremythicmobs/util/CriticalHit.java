package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.stats.StatRegistry;
import io.lumine.mythic.core.skills.stats.StatType;
import io.lumine.mythic.core.skills.stats.Stats;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Critical hits for tDamage. MythicMobs' own CRITICAL_STRIKE_* stats only roll on melee attacks, never
 * on skill damage, so tDamage rolls them itself when the line says {@code crit=true} (off by default).
 * <ul>
 *   <li>chance = {@code critChance} if written, else the caster's CRITICAL_STRIKE_CHANCE; minus the
 *       target's CRITICAL_STRIKE_RESILIENCE in both cases.</li>
 *   <li>multiplier = 1 + ({@code critDamage} if written, else the caster's CRITICAL_STRIKE_DAMAGE).</li>
 * </ul>
 * A stat that is not {@code Enabled: true} in stats.yml counts as 0, as it does for MythicMobs.
 */
public final class CriticalHit {
    private final boolean enabled;
    private final PlaceholderDouble chance;
    private final PlaceholderDouble damage;

    private CriticalHit(boolean enabled, PlaceholderDouble chance, PlaceholderDouble damage) {
        this.enabled = enabled;
        this.chance = chance;
        this.damage = damage;
    }

    public static CriticalHit fromConfig(MythicLineConfig config) {
        boolean enabled = config.getBoolean(new String[]{"crit", "critical"}, false);
        String[] chanceKeys = {"critchance", "crc"};
        String[] damageKeys = {"critdamage", "crd"};
        PlaceholderDouble chance = config.getString(chanceKeys, null) == null ? null
                : config.getPlaceholderDouble(chanceKeys, 0.0D, new String[0]);
        PlaceholderDouble damage = config.getString(damageKeys, null) == null ? null
                : config.getPlaceholderDouble(damageKeys, 0.0D, new String[0]);
        return new CriticalHit(enabled, chance, damage);
    }

    public boolean enabled() {
        return enabled;
    }

    /** The damage multiplier for this hit: 1 when it does not crit. */
    public double roll(SkillMetadata data, AbstractEntity target) {
        if (!enabled) return 1.0D;
        AbstractEntity caster = data.getCaster().getEntity();
        double chanceValue = chance != null ? chance.get(data, target) : stat(caster, Stats.CRITICAL_STRIKE_CHANCE);
        chanceValue -= stat(target, Stats.CRITICAL_STRIKE_RESILIENCE);
        if (chanceValue <= 0.0D || ThreadLocalRandom.current().nextDouble() >= chanceValue) return 1.0D;
        double bonus = damage != null ? damage.get(data, target) : stat(caster, Stats.CRITICAL_STRIKE_DAMAGE);
        return 1.0D + bonus;
    }

    private static double stat(AbstractEntity entity, StatType type) {
        if (!type.isEnabled()) return 0.0D;
        StatRegistry registry = MythicBukkit.inst().getStatManager().getStatRegistry(entity).orElse(null);
        return registry == null ? 0.0D : registry.get(type);
    }
}
