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
import org.bukkit.Material;
import org.bukkit.World;

import java.util.EnumSet;
import java.util.Set;

/**
 * Counts matching blocks within a cubic radius around the entity or location.
 * Usage: ?lmBlockCount{m=WATER,BUBBLE_COLUMN;r=3;c=>=4}
 */
public class BlockCountCondition extends SkillCondition implements IEntityCondition, ILocationCondition {
    private final Set<Material> materials;
    private final int radius;
    private final String countExpr;

    public BlockCountCondition(MythicLineConfig config) {
        super(config.getLine());
        String raw = config.getString(new String[]{"materials", "m", "block", "b"}, "");
        Set<Material> set = EnumSet.noneOf(Material.class);
        for (String s : ConditionMatchUtil.parseCommaSeparated(raw)) {
            Material m = Material.matchMaterial(s);
            if (m != null) set.add(m);
        }
        this.materials = set;
        this.radius = Math.max(0, Math.min(10, config.getInteger(new String[]{"radius", "r"}, 3)));
        this.countExpr = config.getString(new String[]{"count", "c", "value", "v"}, ">=1");
    }

    @Override
    public boolean check(AbstractEntity entity) {
        if (entity == null) return false;
        return check(entity.getLocation());
    }

    @Override
    public boolean check(AbstractLocation target) {
        if (target == null || materials.isEmpty()) return false;
        Location loc = BukkitAdapter.adapt(target);
        World w = loc.getWorld();
        if (w == null) return false;

        int cx = loc.getBlockX(), cy = loc.getBlockY(), cz = loc.getBlockZ();
        int count = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (materials.contains(w.getBlockAt(cx + dx, cy + dy, cz + dz).getType())) {
                        count++;
                    }
                }
            }
        }
        return NumericComparison.matches(count, countExpr);
    }
}
