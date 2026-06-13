package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.core.skills.SkillCondition;
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
