package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityCondition;
import net.azisaba.lifemoremythicmobs.mechanic.TypeBuffMechanic;

public class HasTypeBuffCondition extends SkillCondition implements IEntityCondition {

    private final String auraName;

    public HasTypeBuffCondition(MythicLineConfig config) {
        super(config.getLine());
        this.auraName = config.getString(new String[]{"auraName", "aura", "name", "n"}, "typebuff");
    }

    @Override
    public boolean check(AbstractEntity entity) {
        return TypeBuffMechanic.hasAura(entity.getUniqueId(), auraName);
    }
}