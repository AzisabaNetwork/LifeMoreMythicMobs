package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.core.skills.SkillCondition;
import net.azisaba.lifemoremythicmobs.mechanic.TypeBuffMechanic;

import java.util.List;

public class HasTypeBuffCondition extends SkillCondition implements IEntityCondition {

    private final List<String> auraNames;

    public HasTypeBuffCondition(MythicLineConfig config) {
        super(config.getLine());
        this.auraNames = ConditionMatchUtil.parseCommaSeparated(config.getString(new String[]{"auraName", "aura", "name", "n"}, "typebuff"));
    }

    @Override
    public boolean check(AbstractEntity entity) {
        for (String auraName : auraNames) {
            if (TypeBuffMechanic.hasAura(entity.getUniqueId(), auraName)) {
                return true;
            }
        }
        return false;
    }
}
