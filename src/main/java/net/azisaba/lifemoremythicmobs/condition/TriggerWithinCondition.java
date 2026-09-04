package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.conditions.IEntityComparisonCondition;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.api.skills.conditions.ISkillMetaCondition;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.core.skills.SkillCondition;

public class TriggerWithinCondition extends SkillCondition implements ISkillMetaCondition, IEntityCondition, IEntityComparisonCondition {
    private final PlaceholderDouble distance;
    private final double defaultDistance;
    private final boolean not;

    public TriggerWithinCondition(MythicLineConfig config, boolean not) {
        super(config.getLine());
        this.defaultDistance = config.getDouble(new String[]{"distance", "d", "dist", "r", "radius"}, 5.0);
        this.distance = PlaceholderDouble.of(config.getString(new String[]{"distance", "d", "dist", "r", "radius"}, "5.0"));
        this.not = not;
    }

    public TriggerWithinCondition(MythicLineConfig config) {
        this(config, false);
    }

    @Override
    public boolean check(SkillMetadata meta) {
        if (meta == null || meta.getCaster() == null || meta.getTrigger() == null) {
            return not;
        }
        double d = this.distance.get(meta);
        boolean within = meta.getCaster().getLocation().distanceSquared(meta.getTrigger().getLocation()) <= d * d;
        return not ? !within : within;
    }

    @Override
    public boolean check(AbstractEntity entity) {
        return true;
    }

    @Override
    public boolean check(AbstractEntity caster, AbstractEntity target) {
        if (caster == null || target == null) {
            return not;
        }
        double d = this.defaultDistance;
        boolean within = caster.getLocation().distanceSquared(target.getLocation()) <= d * d;
        return not ? !within : within;
    }
}
