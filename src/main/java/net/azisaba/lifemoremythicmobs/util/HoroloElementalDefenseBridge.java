package net.azisaba.lifemoremythicmobs.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/** Optional, reflection-only bridge so LifeMoreMythicMobs remains usable without HoroloCore. */
public final class HoroloElementalDefenseBridge {
    private static Plugin cachedPlugin;
    private static Method cachedMethod;
    private static Method cachedDamageMethod;

    private HoroloElementalDefenseBridge() {
    }

    public static double outgoingDamage(Player player, String element) {
        if (player == null || element == null || element.isBlank()) return 0.0D;
        try {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("HoroloCore");
            if (plugin == null || !plugin.isEnabled()) { clearCache(); return 0.0D; }
            if (plugin != cachedPlugin || cachedDamageMethod == null) {
                cachedPlugin = plugin;
                cachedDamageMethod = plugin.getClass().getMethod("getOrbElementalDamage", java.util.UUID.class, String.class);
            }
            Object value = cachedDamageMethod.invoke(plugin, player.getUniqueId(), element);
            return value instanceof Number number && Double.isFinite(number.doubleValue()) ? number.doubleValue() : 0.0D;
        } catch (ReflectiveOperationException | RuntimeException ignored) { clearCache(); return 0.0D; }
    }

    public static Stats get(Player player, String element) {
        if (player == null || element == null || element.isBlank()) {
            return Stats.ZERO;
        }

        try {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("HoroloCore");
            if (plugin == null || !plugin.isEnabled()) {
                clearCache();
                return Stats.ZERO;
            }
            if (plugin != cachedPlugin || cachedMethod == null) {
                cachedPlugin = plugin;
                cachedMethod = plugin.getClass().getMethod(
                        "getElementalDefense", java.util.UUID.class, String.class);
            }
            Object value = cachedMethod.invoke(plugin, player.getUniqueId(), element);
            if (value instanceof double[] stats && stats.length >= 2) {
                double armor = Double.isFinite(stats[0]) ? stats[0] : 0.0D;
                double toughness = Double.isFinite(stats[1]) ? stats[1] : 0.0D;
                return new Stats(armor, toughness);
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            clearCache();
        }
        return Stats.ZERO;
    }

    private static void clearCache() {
        cachedPlugin = null;
        cachedMethod = null;
        cachedDamageMethod = null;
    }

    public record Stats(double armor, double toughness) {
        public static final Stats ZERO = new Stats(0.0D, 0.0D);
    }
}
