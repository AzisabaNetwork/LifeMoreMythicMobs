package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.skills.triggers.meta.DamagedMetadata;
import io.lumine.mythic.core.skills.triggers.meta.EntityAttackMetadata;

/**
 * {@code scaleDamage{m=0.7}} in an {@code ~onAttack} / {@code ~onDamaged} skill: multiplies the whole damage of
 * this hit (elements included). Unlike modifyDamage it also works on damage without an attacker (fall, fire and
 * so on), so "take 30% less damage" covers every source.
 */
public class ScaleDamageMechanic extends SkillMechanic implements INoTargetSkill {
    private final PlaceholderDouble multiplier;

    public ScaleDamageMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, null, config.getLine(), config);
        this.multiplier = config.getPlaceholderDouble(new String[]{"multiplier", "m", "amount", "a"}, 1.0D);
    }

    @Override
    public SkillResult cast(SkillMetadata data) {
        if (data.isAsync()) {
            return SkillResult.INVALID_CONFIG;
        }
        double m = multiplier.get(data);
        if (!Double.isFinite(m)) {
            return SkillResult.INVALID_CONFIG;
        }
        if (data.getTriggerMetadata() instanceof EntityAttackMetadata meta) {
            meta.setDamageModifier(meta.getDamageModifier() * m);
            return SkillResult.SUCCESS;
        }
        if (data.getTriggerMetadata() instanceof DamagedMetadata meta) {
            meta.setDamage(meta.getDamage() * m);
            return SkillResult.SUCCESS;
        }
        return SkillResult.INVALID_CONFIG;
    }
}
