package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.core.skills.SkillCondition;
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
