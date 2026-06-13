package net.azisaba.lifemoremythicmobs.commands;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.upgrade.UpgradeStatManager;
import net.azisaba.lifemoremythicmobs.upgrade.UpgradeStatManager.StatType;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StatsCommand extends SubCommand {

    private final LifeMoreMythicMobs plugin;

    public StatsCommand(LifeMoreMythicMobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "stats";
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull [] args) {
        player.sendMessage(ChatColor.GOLD + "========== [ ステータス ] ==========");

        // 全てのバニラ属性の表示
        for (Attribute attribute : Registry.ATTRIBUTE) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                String label = getAttributeLabel(attribute);
                double value = instance.getValue();
                String formatted = String.format("%.2f", value);
                player.sendMessage(ChatColor.YELLOW + label + ": " + ChatColor.WHITE + formatted);
            }
        }

        // サーバー名が lifeevent の場合のみ Upgrade 属性を表示
        String serverName = LifeMoreMythicMobs.inst().server;
        if (serverName.equalsIgnoreCase("lifeevent")) {
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "--- [ 能力強化 (Upgrade) ] ---");

            // Upgrade 属性の表示
            for (StatType type : StatType.values()) {
                int level = 0;
                for (String profile : UpgradeStatManager.ALL_PROFILES) {
                    level += UpgradeStatManager.getLevel(player, profile, type);
                }
                double boost = level * type.perLevel * 100.0;
                player.sendMessage(ChatColor.YELLOW + type.displayName + ": " + ChatColor.WHITE + level + " Lv " + ChatColor.GRAY + "(+" + (int) boost + "%)");
            }
        }

        player.sendMessage(ChatColor.GOLD + "====================================");
    }

    private String getAttributeLabel(Attribute attribute) {
        String key = attribute.getKey().getKey(); // e.g. "max_health"
        switch (key) {
            case "max_health": return "最大体力";
            case "follow_range": return "追跡範囲";
            case "knockback_resistance": return "ノックバック耐性";
            case "movement_speed": return "移動速度";
            case "attack_damage": return "攻撃力";
            case "attack_speed": return "攻撃速度";
            case "armor": return "防御力";
            case "armor_toughness": return "防具強度";
            case "luck": return "幸運";
            default:
                // 内部名を見やすく整形 (例: flying_speed -> Flying Speed)
                String name = key.replace("_", " ").toLowerCase();
                if (name.length() > 0) {
                    name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                }
                return name;
        }
    }
}
