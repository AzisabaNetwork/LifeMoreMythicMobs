package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.api.skills.conditions.ILocationCondition;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.SkillCondition;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Checks whether the entity or location has an unobstructed view of the sky (highest block Y <= current Y).
 * Usage: ?lmCanSeeSky
 */
public class CanSeeSkyCondition extends SkillCondition implements IEntityCondition, ILocationCondition {
    public CanSeeSkyCondition(MythicLineConfig config) {
        super(config.getLine());
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
        World w = loc.getWorld();
        if (w == null) return false;
        int highest = w.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ());
        return loc.getBlockY() >= highest;
    }
}
