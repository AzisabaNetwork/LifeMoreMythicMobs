package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;

public class RemoveCustomAuraMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final String auraName;
    protected final int stacks;

    public RemoveCustomAuraMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.auraName = config.getString(new String[]{"auraName", "n", "名前"}, "default");
        this.stacks = config.getInteger(new String[]{"stacks", "s"}, -1);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (stacks < 0) {
            // 全スタック削除
            OnDeathAuraMechanic.remove(target, auraName);
            OnKillAuraMechanic.remove(target, auraName);
            NullRecoveryMechanic.remove(target, auraName);
            ModifyPlayerAttributeMechanic.remove(target, auraName);
            TypeBuffMechanic.remove(target, auraName);
            TypeOffensiveBuffMechanic.remove(target, auraName);
            OnConsumeAuraMechanic.remove(target, auraName);
        } else {
            // 指定数のスタックのみ削除（TypeBuffのみstackをサポート）
            TypeBuffMechanic.remove(target, auraName, stacks);
        }
        return SkillResult.SUCCESS;
    }
}