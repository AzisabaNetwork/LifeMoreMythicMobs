package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.skills.triggers.meta.EntityAttackMetadata;
import net.azisaba.lifemoremythicmobs.util.CritRoll;

/**
 * {@code critChance{chance=0.05;multiplier=1.5;pvp=false}} in an {@code ~onAttack} skill: adds a critical
 * chance to this hit. Every chance on the hit, plus the attacker's {@code critChanceBuff}, is summed and
 * rolled once; on a crit the damage is multiplied by the highest multiplier. Skipped on players unless
 * {@code pvp=true}.
 */
public class CritChanceMechanic extends SkillMechanic implements INoTargetSkill {
    private final PlaceholderDouble chance;
    private final PlaceholderDouble multiplier;
    private final boolean pvp;

    public CritChanceMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, null, config.getLine(), config);
        this.chance = config.getPlaceholderDouble(new String[]{"chance", "c"}, 0.0D);
        this.multiplier = config.getPlaceholderDouble(new String[]{"multiplier", "m"}, 1.5D);
        this.pvp = config.getBoolean(new String[]{"pvp"}, false);
    }

    @Override
    public SkillResult cast(SkillMetadata data) {
        if (!(data.getTriggerMetadata() instanceof EntityAttackMetadata meta)) {
            return SkillResult.INVALID_CONFIG;
        }
        if (meta.getDamager() == null) {
            return SkillResult.INVALID_TARGET;
        }
        if (!pvp && meta.getDamaged() != null && meta.getDamaged().isPlayer()) {
            return SkillResult.CONDITION_FAILED;
        }
        CritRoll.of(meta, meta.getDamager().getUniqueId()).add(chance.get(data), multiplier.get(data));
        return SkillResult.SUCCESS;
    }
}
