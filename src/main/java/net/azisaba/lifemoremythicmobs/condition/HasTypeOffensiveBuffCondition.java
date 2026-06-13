package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityCondition;
import net.azisaba.lifemoremythicmobs.mechanic.TypeOffensiveBuffMechanic;

public class HasTypeOffensiveBuffCondition extends SkillCondition implements IEntityCondition {

    private final String auraName;

    public HasTypeOffensiveBuffCondition(MythicLineConfig config) {
        super(config.getLine());
        this.auraName = config.getString(new String[]{"auraName", "aura", "name", "n"}, "typeoffensivebuff");
    }

    @Override
    public boolean check(AbstractEntity entity) {
        return TypeOffensiveBuffMechanic.hasAura(entity.getUniqueId(), auraName);
    }
}
