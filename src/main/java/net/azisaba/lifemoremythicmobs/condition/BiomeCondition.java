package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.api.skills.conditions.ILocationCondition;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.SkillCondition;
import org.bukkit.Location;
import org.bukkit.block.Biome;

import java.util.List;

/**
 * Checks whether the entity or location is in one of the specified biomes.
 * Usage: ?lmBiome{b=PLAINS,FOREST,TAIGA}
 */
public class BiomeCondition extends SkillCondition implements IEntityCondition, ILocationCondition {
    private final List<String> biomes;

    public BiomeCondition(MythicLineConfig config) {
        super(config.getLine());
        this.biomes = ConditionMatchUtil.parseCommaSeparated(config.getString(new String[]{"biome", "biomes", "b"}, ""));
    }

    @Override
    public boolean check(AbstractEntity entity) {
        if (entity == null) return false;
        return check(entity.getLocation());
    }

    @Override
    public boolean check(AbstractLocation target) {
        if (target == null || biomes.isEmpty()) return false;
        Location loc = BukkitAdapter.adapt(target);
        if (loc.getWorld() == null) return false;
        Biome biome = loc.getWorld().getBiome(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        String key = biome.getKey().getKey();
        String fullKey = biome.getKey().toString();
        return ConditionMatchUtil.anyEquals(key, biomes, true) || ConditionMatchUtil.anyEquals(fullKey, biomes, true);
    }
}
