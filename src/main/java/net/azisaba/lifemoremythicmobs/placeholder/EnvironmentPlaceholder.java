package net.azisaba.lifemoremythicmobs.placeholder;

import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;
import io.lumine.mythic.bukkit.BukkitAdapter;
import net.azisaba.lifemoremythicmobs.condition.UsingSpearCondition;
import net.azisaba.lifemoremythicmobs.util.PlaceholderFactory;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * Registers environment placeholders:
 * <caster.lmmm.biome>, <target.lmmm.biome>, <caster.lmmm.light>, <target.lmmm.light>,
 * <caster.lmmm.spear_ticks>, <target.lmmm.spear_ticks>
 */
public class EnvironmentPlaceholder {
    public static void register(PlaceholderManager manager) {
        manager.register("caster.lmmm.biome", PlaceholderFactory.meta((meta, arg) -> {
            if (meta.getCaster() == null || meta.getCaster().getEntity() == null) return "";
            return getBiomeKey(BukkitAdapter.adapt(meta.getCaster().getEntity().getLocation()));
        }));
        manager.register("target.lmmm.biome", PlaceholderFactory.target((meta, entity, arg) -> {
            if (entity == null) return "";
            return getBiomeKey(BukkitAdapter.adapt(entity.getLocation()));
        }));
        manager.register("caster.lmmm.light", PlaceholderFactory.meta((meta, arg) -> {
            if (meta.getCaster() == null || meta.getCaster().getEntity() == null) return "0";
            return getLight(BukkitAdapter.adapt(meta.getCaster().getEntity().getLocation()));
        }));
        manager.register("target.lmmm.light", PlaceholderFactory.target((meta, entity, arg) -> {
            if (entity == null) return "0";
            return getLight(BukkitAdapter.adapt(entity.getLocation()));
        }));
        manager.register("caster.lmmm.spear_ticks", PlaceholderFactory.meta((meta, arg) -> {
            if (meta.getCaster() == null || meta.getCaster().getEntity() == null) return "0";
            return getSpearTicks(BukkitAdapter.adapt(meta.getCaster().getEntity()));
        }));
        manager.register("target.lmmm.spear_ticks", PlaceholderFactory.target((meta, entity, arg) -> {
            if (entity == null) return "0";
            return getSpearTicks(BukkitAdapter.adapt(entity));
        }));
    }

    private static String getSpearTicks(Entity entity) {
        if (!(entity instanceof LivingEntity living)) return "0";
        if (!living.hasActiveItem() && !living.isHandRaised()) return "0";
        if (!UsingSpearCondition.isSpearItem(living.getActiveItem(), false, true)) return "0";
        return String.valueOf(living.getActiveItemUsedTime());
    }

    private static String getBiomeKey(Location loc) {
        if (loc == null || loc.getWorld() == null) return "";
        return loc.getWorld().getBiome(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).getKey().getKey();
    }

    private static String getLight(Location loc) {
        if (loc == null || loc.getWorld() == null) return "0";
        return String.valueOf(loc.getBlock().getLightLevel());
    }
}
