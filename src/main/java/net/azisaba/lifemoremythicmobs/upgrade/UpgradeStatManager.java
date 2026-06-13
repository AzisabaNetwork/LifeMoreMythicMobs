package net.azisaba.lifemoremythicmobs.upgrade;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.skills.variables.VariableRegistry;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class UpgradeStatManager {

    private static final UUID ATTRIBUTE_UUID = UUID.fromString("f0a1b2c3-d4e5-4f6a-8b9c-0d1e2f3a4b5c");
    public static final List<String> ALL_PROFILES = Arrays.asList("hw2026");
    public static final int MAX_LEVEL = 100;
    
    public enum StatType {
        // HW2026 イベント系
        FIRE_DMG(0.01, "炎属性強化"),
        FIRE_RES(0.01, "炎属性耐性"),
        WATER_DMG(0.01, "水属性強化"),
        WATER_RES(0.01, "水属性耐性"),
        LEAF_DMG(0.01, "草属性強化"),
        LEAF_RES(0.01, "草属性耐性");

        public final double perLevel;
        public final String displayName;

        StatType(double perLevel, String displayName) {
            this.perLevel = perLevel;
            this.displayName = displayName;
        }
    }

    public static void updateAllStats(Player player) {
        VariableRegistry registry = MythicMobs.inst().getPlayerManager().getPlayerData(BukkitAdapter.adapt(player)).getVariables();
        
        boolean isLifeEvent = LifeMoreMythicMobs.inst().server.equalsIgnoreCase("lifeevent");

        for (StatType type : StatType.values()) {
            double totalLevel = 0;
            // lifeevent サーバーの場合のみレベルを合算
            if (isLifeEvent) {
                for (String profile : ALL_PROFILES) {
                    String varName = "upg_" + profile + "_" + type.name().toLowerCase() + "_lv";
                    if (registry.has(varName)) {
                        totalLevel += registry.getInt(varName);
                    }
                }
            }

            // 合計レベルを変数にキャッシュ (メカニクス用)
            // TypedDamageMechanic との整合性のため、"upg_total_" + type.name().toLowerCase() を使用
            // 例: FIRE_DMG -> upg_total_fire_dmg
            registry.putInt("upg_total_" + type.name().toLowerCase(), (int) totalLevel);
        }
    }

    public static int getLevel(Player player, String profile, StatType type) {
        VariableRegistry registry = MythicMobs.inst().getPlayerManager().getPlayerData(BukkitAdapter.adapt(player)).getVariables();
        String varName = "upg_" + profile + "_" + type.name().toLowerCase() + "_lv";
        return registry.has(varName) ? registry.getInt(varName) : 0;
    }

    public static void setLevel(Player player, String profile, StatType type, int level) {
        VariableRegistry registry = MythicMobs.inst().getPlayerManager().getPlayerData(BukkitAdapter.adapt(player)).getVariables();
        String varName = "upg_" + profile + "_" + type.name().toLowerCase() + "_lv";
        registry.putInt(varName, level);
        updateAllStats(player);
    }
    
    public static void clearProfile(Player player, String profile) {
        VariableRegistry registry = MythicMobs.inst().getPlayerManager().getPlayerData(BukkitAdapter.adapt(player)).getVariables();
        for (StatType type : StatType.values()) {
            String varName = "upg_" + profile + "_" + type.name().toLowerCase() + "_lv";
            registry.remove(varName);
        }
        updateAllStats(player);
    }
}
