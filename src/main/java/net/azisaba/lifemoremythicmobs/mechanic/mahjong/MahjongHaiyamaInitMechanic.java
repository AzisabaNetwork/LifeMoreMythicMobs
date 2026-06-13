package net.azisaba.lifemoremythicmobs.mechanic.mahjong;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;

public class MahjongHaiyamaInitMechanic extends SkillMechanic implements ITargetedEntitySkill {
    public MahjongHaiyamaInitMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
    }
    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        return SkillResult.SUCCESS;
    }
}
