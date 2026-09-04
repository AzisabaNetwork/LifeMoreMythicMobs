package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.conditions.ICasterCondition;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.api.skills.conditions.ILocationCondition;
import io.lumine.mythic.api.skills.conditions.ISkillMetaCondition;
import io.lumine.mythic.core.skills.SkillCondition;

public class WarmupCondition extends SkillCondition implements ISkillMetaCondition, ICasterCondition, IEntityCondition, ILocationCondition {
    public WarmupCondition(MythicLineConfig config) {
        super(config.getLine());
    }

    @Override
    public boolean check(SkillMetadata meta) {
        return true;
    }

    @Override
    public boolean check(SkillCaster caster) {
        return true;
    }

    @Override
    public boolean check(AbstractEntity entity) {
        return true;
    }

    @Override
    public boolean check(AbstractLocation location) {
        return true;
    }
}
