package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityCondition;
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
