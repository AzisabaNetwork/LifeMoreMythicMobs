package net.azisaba.lifemoremythicmobs.commands;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.LegacyText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class FindMythicMobCommand extends SubCommand {

    private final LifeMoreMythicMobs plugin;
    private static final List<String> KEYS = Arrays.asList("name=", "type=", "health=");

    public FindMythicMobCommand(LifeMoreMythicMobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "findMythicMob";
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String[] args) {
        if (args.length == 0) {
            player.sendMessage(LegacyText.RED + "使用法: /lmmm findMythicMob <表示名 | name=表示名 | type=モブタイプ | health=HP>");
            return;
        }
        player.sendMessage(LegacyText.YELLOW + "Mobを検索しています...");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<MythicMob> matchedMobs = new ArrayList<>();
            Collection<MythicMob> mobTypes = MythicBukkit.inst().getMobManager().getMobTypes();
            for (MythicMob mob : mobTypes) {
                boolean isMatch = true;
                for (String arg : args) {
                    String lowerArg = arg.toLowerCase();
                    if (lowerArg.startsWith("name=")) {
                        String val = lowerArg.substring(5);
                        if (!checkNameMatch(mob, val)) isMatch = false;
                    } else if (lowerArg.startsWith("type=")) {
                        String queryType = lowerArg.substring(5).toUpperCase();
                        String mobType = mob.getEntityType() != null ? mob.getEntityType().toString().toUpperCase() : "UNKNOWN";
                        if (!mobType.contains(queryType)) isMatch = false;
                    } else if (lowerArg.startsWith("health=")) {
                        try {
                            double queryHealth = Double.parseDouble(lowerArg.substring(7));
                            if (mob.getHealth() != null && mob.getHealth().get() != queryHealth) isMatch = false;
                        } catch (Exception e) {
                            isMatch = false;
                        }
                    } else {
                        if (!checkNameMatch(mob, lowerArg)) isMatch = false;
                    }
                    if (!isMatch) break;
                }
                if (isMatch) matchedMobs.add(mob);
            }
            Bukkit.getScheduler().runTask(plugin, () -> sendResult(player, matchedMobs));
        });
    }

    private void sendResult(Player player, List<MythicMob> mobs) {
        if (mobs.isEmpty()) {
            player.sendMessage(LegacyText.YELLOW + "該当するMobが見つかりませんでした。");
            return;
        }
        player.sendMessage(LegacyText.GREEN + "=== Mob検索結果 (" + mobs.size() + "件) ===");
        for (MythicMob mob : mobs) {
            String internalName = mob.getInternalName();
            String displayName = mob.getDisplayName() != null ? mob.getDisplayName().get() : "No DisplayName";
            double hp = mob.getHealth().get();
            String entityType = mob.getEntityType() != null ? mob.getEntityType().toString() : "UNKNOWN";

            String command = "/mm eggs give " + player.getName() + " " + internalName;
            Component message = LegacyText.component(LegacyText.GOLD + internalName + LegacyText.GRAY + " (" + entityType + " / " + hp + "HP)" + LegacyText.WHITE + " -> ")
                    .append(LegacyText.ampersandComponent(displayName))
                    .hoverEvent(HoverEvent.showText(LegacyText.component(LegacyText.YELLOW + "ID: " + internalName + "\n" + LegacyText.GREEN + "クリックでスポーンエッグを入手")))
                    .clickEvent(ClickEvent.runCommand(command));
            player.sendMessage(message);
        }
    }

    private boolean checkNameMatch(MythicMob mob, String query) {
        if (mob.getDisplayName() == null) return false;
        String displayName = mob.getDisplayName().get();
        if (displayName == null) return false;
        String stripped = LegacyText.stripColor(LegacyText.translateAlternateColorCodes('&', displayName)).toLowerCase();
        return stripped.contains(query.toLowerCase());
    }

    @Override
    public @NotNull List<String> suggest(@NotNull Player player, @NotNull String[] args) {
        String currentArg = args[args.length - 1].toLowerCase();
        List<String> suggestions = new ArrayList<>();
        for (String key : KEYS) {
            if (key.startsWith(currentArg)) {
                suggestions.add(key);
            }
        }
        if (currentArg.startsWith("type=")) {
            String val = currentArg.substring(5);
            return Arrays.stream(EntityType.values())
                    .filter(EntityType::isSpawnable)
                    .map(type -> "type=" + type.name().toLowerCase())
                    .filter(s -> s.startsWith("type=" + val))
                    .collect(Collectors.toList());
        }
        return suggestions;
    }
}
