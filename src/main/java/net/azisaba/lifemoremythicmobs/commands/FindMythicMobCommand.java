package net.azisaba.lifemoremythicmobs.commands;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FindMythicMobCommand extends SubCommand {

    private static final List<String> KEYS = Arrays.asList("name=", "type=", "health=");

    @Override
    public @NotNull String getName() {
        return "findMythicMob";
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "使用法: /lmmm findMythicMob <表示名 | name=表示名 | type=モブタイプ | health=HP>");
            return;
        }
        List<MythicMob> matchedMobs = new ArrayList<>();
        for (MythicMob mob : MythicMobs.inst().getMobManager().getMobTypes()) {
            boolean isMatch = true;

            for (String arg : args) {
                String lowerArg = arg.toLowerCase();

                if (lowerArg.startsWith("name=")) {
                    String val = lowerArg.substring(5);
                    if (!checkNameMatch(mob, val)) isMatch = false;

                } else if (lowerArg.startsWith("type=")) {
                    String type = mob.getEntityType() != null ? mob.getEntityType().toUpperCase() : "UNKNOWN";
                    if (!type.contains(lowerArg.substring(5).toUpperCase())) isMatch = false;

                } else if (lowerArg.startsWith("health=")) {
                    try {
                        double val = Double.parseDouble(lowerArg.substring(7));
                        if (mob.getHealth().get() != val) isMatch = false;
                    } catch (NumberFormatException e) {
                        isMatch = false;
                    }
                } else {
                    if (!checkNameMatch(mob, lowerArg)) isMatch = false;
                }
                if (!isMatch) break;
            }
            if (isMatch) {
                matchedMobs.add(mob);
            }
        }

        if (matchedMobs.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "該当するMobが見つかりませんでした");
        } else {
            player.sendMessage(ChatColor.GREEN + "=== Mob検索結果 (" + matchedMobs.size() + "件) ===");
            for (MythicMob mob : matchedMobs) {
                String name = mob.getDisplayName() != null ? mob.getDisplayName().get() : "No DisplayName";
                double hp = mob.getHealth() != null ? mob.getHealth().get() : 0;
                player.sendMessage(ChatColor.GOLD + mob.getInternalName() +
                        ChatColor.GRAY + " (" + mob.getEntityType() + " / " + hp + "HP)" +
                        ChatColor.WHITE + " -> " + name);
            }
        }
    }

    private boolean checkNameMatch(MythicMob mob, String query) {
        String displayName = mob.getDisplayName() != null ? mob.getDisplayName().get() : "";
        String stripped = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', displayName)).toLowerCase();
        return stripped.contains(query);
    }

    @Override
    public @NotNull List<@NotNull String> suggest(@NotNull Player player, @NotNull String @NotNull [] args) {
        String currentArg = args[args.length - 1].toLowerCase();
        List<String> suggestions = new ArrayList<>();
        List<String> usedKeys = new ArrayList<>();
        for (int i = 0; i < args.length - 1; i++) {
            String arg = args[i].toLowerCase();
            for (String key : KEYS) {
                if (arg.startsWith(key)) usedKeys.add(key);
            }
        }
        for (String key : KEYS) {
            if (!usedKeys.contains(key) && key.startsWith(currentArg)) suggestions.add(key);
        }
        if (currentArg.startsWith("type=")) {
            String val = currentArg.substring(5);
            suggestions.addAll(Arrays.stream(EntityType.values())
                    .filter(EntityType::isSpawnable)
                    .map(type -> "type=" + type.name().toLowerCase())
                    .filter(s -> s.startsWith("type=" + val))
                    .collect(Collectors.toList()));
        }
        return suggestions;
    }
}