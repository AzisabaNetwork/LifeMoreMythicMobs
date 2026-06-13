package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;

public class RemoveCustomAuraMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final String auraName;

    public RemoveCustomAuraMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.auraName = config.getString(new String[]{"auraName", "n", "名前"}, "default");
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        OnDeathAuraMechanic.remove(target, auraName);
        OnKillAuraMechanic.remove(target, auraName);
        OnConsumeAuraMechanic.remove(target, auraName);
        NullRecoveryMechanic.remove(target, auraName);
        ModifyAttributeMechanic.remove(target, auraName);
        TypeBuffMechanic.remove(target, auraName);
        return true;
    }
}