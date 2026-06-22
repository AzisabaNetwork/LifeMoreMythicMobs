package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;

public class RemoveCustomAuraMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final String auraName;
    protected final int stacks;

    public RemoveCustomAuraMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.auraName = config.getString(new String[]{"auraName", "n", "名前"}, "default");
        this.stacks = config.getInteger(new String[]{"stacks", "s"}, -1);
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
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
        return true;
    }
}