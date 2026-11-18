package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.core.skills.SkillCondition;
import net.azisaba.lifemoremythicmobs.util.MarkStackManager;
import net.azisaba.lifemoremythicmobs.util.NumericComparison;

/**
 * Checks whether an entity has a specific number of mark stacks.
 * Usage: ?lmMarkStacks{id=burn;v=>=3}
 */
public class MarkStacksCondition extends SkillCondition implements IEntityCondition {
    private final String markId;
    private final String expr;

    public MarkStacksCondition(MythicLineConfig config) {
        super(config.getLine());
        this.markId = config.getString(new String[]{"id", "mark", "m"}, "mark");
        this.expr = config.getString(new String[]{"value", "v", "stacks", "s"}, ">=1");
    }

    @Override
    public boolean check(AbstractEntity entity) {
        if (entity == null) return false;
        int stacks = MarkStackManager.getStacks(entity.getUniqueId(), markId);
        return NumericComparison.matches(stacks, expr);
    }
}
