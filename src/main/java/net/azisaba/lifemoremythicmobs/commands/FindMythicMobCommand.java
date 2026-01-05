package net.azisaba.lifemoremythicmobs.commands;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    public void execute(@NotNull Player player, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "使用法: /lmmm findMythicMob <表示名 | name=表示名 | type=モブタイプ | health=HP>");
            return;
        }
        player.sendMessage(ChatColor.YELLOW + "Mobを検索しています...");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<MythicMob> matchedMobs = new ArrayList<>();
            Collection<MythicMob> mobTypes = MythicMobs.inst().getMobManager().getMobTypes();
            for (MythicMob mob : mobTypes) {
                boolean isMatch = true;
                for (String arg : args) {
                    String lowerArg = arg.toLowerCase();
                    if (lowerArg.startsWith("name=")) {
                        String val = lowerArg.substring(5);
                        if (!checkNameMatch(mob, val)) isMatch = false;
                    } else if (lowerArg.startsWith("type=")) {
                        String queryType = lowerArg.substring(5).toUpperCase();
                        String mobType = mob.getEntityType() != null ? mob.getEntityType().toUpperCase() : "UNKNOWN";
                        if (!mobType.contains(queryType)) isMatch = false;
                    } else if (lowerArg.startsWith("health=")) {
                        try {
                            double val = Double.parseDouble(lowerArg.substring(7));
                            if (mob.getHealth() != null && mob.getHealth().get() != val) isMatch = false;
                        } catch (Exception e) {
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
            Bukkit.getScheduler().runTask(plugin, () -> {
                sendResult(player, matchedMobs);
            });
        });
    }

    private void sendResult(Player player, List<MythicMob> mobs) {
        if (mobs.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "該当するMobが見つかりませんでした。");
        } else {
            player.sendMessage(ChatColor.GREEN + "=== Mob検索結果 (" + mobs.size() + "件) ===");
            int count = 0;
            for (MythicMob mob : mobs) {
                String internalName = mob.getInternalName();
                String displayName = "No DisplayName";
                if (mob.getDisplayName() != null) {
                    displayName = mob.getDisplayName().get();
                }
                double hp = (mob.getHealth() != null) ? mob.getHealth().get() : 0;
                String entityType = (mob.getEntityType() != null) ? mob.getEntityType() : "UNKNOWN";
                TextComponent mainComponent = new TextComponent(ChatColor.GOLD + internalName);
                TextComponent infoComponent = new TextComponent(ChatColor.GRAY + " (" + entityType + " / " + hp + "HP)");
                TextComponent nameComponent = new TextComponent(ChatColor.WHITE + " -> " + ChatColor.translateAlternateColorCodes('&', displayName));
                ComponentBuilder hover = new ComponentBuilder(ChatColor.YELLOW + "ID: " + internalName + "\n");
                hover.append(ChatColor.GREEN + "クリックでスポーンエッグを入手");
                mainComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.create()));
                String command = "/mm eggs give " + player.getName() + " " + internalName;
                mainComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
                mainComponent.addExtra(infoComponent);
                mainComponent.addExtra(nameComponent);
                player.spigot().sendMessage(mainComponent);
                count++;
            }
        }
    }

    private boolean checkNameMatch(MythicMob mob, String query) {
        if (mob.getDisplayName() == null) return false;
        String displayName = mob.getDisplayName().get();
        if (displayName == null) return false;
        String stripped = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', displayName)).toLowerCase();
        return stripped.contains(query);
    }

    @Override
    public @NotNull List<@NotNull String> suggest(@NotNull Player player, @NotNull String @NotNull [] args) {
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