package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.api.skills.conditions.ILocationCondition;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.SkillCondition;
import net.azisaba.lifemoremythicmobs.util.NumericComparison;
import org.bukkit.Location;

/**
 * Checks block/sky light level (0..15) at the entity or location.
 * Usage: ?lmLightLevel{l=<=7;type=TOTAL} (type: TOTAL, BLOCK, SKY)
 */
public class LightLevelCondition extends SkillCondition implements IEntityCondition, ILocationCondition {
    public enum LightType {
        TOTAL, BLOCK, SKY
    }

    private final String expr;
    private final LightType type;

    public LightLevelCondition(MythicLineConfig config) {
        super(config.getLine());
        this.expr = config.getString(new String[]{"level", "l", "value", "v"}, "<=7");
        LightType parsed = LightType.TOTAL;
        try {
            parsed = LightType.valueOf(config.getString(new String[]{"type", "t"}, "TOTAL").toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }
        this.type = parsed;
    }

    @Override
    public boolean check(AbstractEntity entity) {
        if (entity == null) return false;
        return check(entity.getLocation());
    }

    @Override
    public boolean check(AbstractLocation target) {
        if (target == null) return false;
        Location loc = BukkitAdapter.adapt(target);
        if (loc.getWorld() == null) return false;
        var block = loc.getBlock();
        int level = switch (type) {
            case BLOCK -> block.getLightFromBlocks();
            case SKY -> block.getLightFromSky();
            case TOTAL -> block.getLightLevel();
        };
        return NumericComparison.matches(level, expr);
    }
}
