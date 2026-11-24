package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import net.azisaba.lifemoremythicmobs.util.CritRoll;

/**
 * {@code critChanceBuff{chance=0.5;d=1200;multiplier=1.5;id=default}}: for {@code d} ticks the target's melee
 * attacks on non-players get this critical chance on top of {@code critChance}. The same id overwrites.
 */
public class CritChanceBuffMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private final PlaceholderDouble chance;
    private final PlaceholderDouble multiplier;
    private final PlaceholderInt duration;
    private final String id;

    public CritChanceBuffMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, null, config.getLine(), config);
        this.chance = config.getPlaceholderDouble(new String[]{"chance", "c"}, 0.0D);
        this.multiplier = config.getPlaceholderDouble(new String[]{"multiplier", "m"}, 1.5D);
        this.duration = config.getPlaceholderInteger(new String[]{"duration", "d"}, 200);
        this.id = config.getString(new String[]{"id", "name"}, "default");
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        CritRoll.addBuff(target.getUniqueId(), id, chance.get(data, target), multiplier.get(data, target),
                duration.get(data, target) * 50L);
        return SkillResult.SUCCESS;
    }
}
