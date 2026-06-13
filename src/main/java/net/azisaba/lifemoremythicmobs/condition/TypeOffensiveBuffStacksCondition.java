package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.core.skills.SkillCondition;
import net.azisaba.lifemoremythicmobs.mechanic.TypeOffensiveBuffMechanic;

public class TypeOffensiveBuffStacksCondition extends SkillCondition implements IEntityCondition {

    private final String auraName;
    private final int min;
    private final int max;

    public TypeOffensiveBuffStacksCondition(MythicLineConfig config) {
        super(config.getLine());
        this.auraName = config.getString(new String[]{"auraName", "aura", "name", "n"}, "typeoffensivebuff");
        this.min = config.getInteger(new String[]{"min", "minStacks"}, 1);
        this.max = config.getInteger(new String[]{"max", "maxStacks"}, Integer.MAX_VALUE);
    }

    @Override
    public boolean check(AbstractEntity entity) {
        int stacks = TypeOffensiveBuffMechanic.getStacks(entity.getUniqueId(), auraName);
        return stacks >= min && stacks <= max;
    }
}
