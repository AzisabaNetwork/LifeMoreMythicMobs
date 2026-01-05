package net.azisaba.lifemoremythicmobs.commands;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class FindMythicItemCommand extends SubCommand {

    private final LifeMoreMythicMobs plugin;
    private static final List<String> KEYS = Arrays.asList("name=", "lore=", "model=", "data=", "material=", "type=", "enchant=");

    public FindMythicItemCommand(LifeMoreMythicMobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "findMythicItem";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "使用法: /lmmm findMythicItem <name=アイテム名 | model=モデル番号 | material=アイテムID | enchant=エンチャント名>");
            return;
        }
        player.sendMessage(ChatColor.YELLOW + "アイテムを検索しています...");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<MythicItem> matchedItems = new ArrayList<>();
            Collection<MythicItem> allItems = MythicMobs.inst().getItemManager().getItems();
            for (MythicItem mmItem : allItems) {
                ItemStack stack = null;
                try {
                    stack = BukkitAdapter.adapt(mmItem.generateItemStack(1));
                } catch (Exception e) {
                    continue;
                }
                if (stack == null || stack.getType() == Material.AIR) continue;
                boolean isMatch = true;
                ItemMeta meta = stack.getItemMeta();
                for (String arg : args) {
                    String lowerArg = arg.toLowerCase();
                    if (lowerArg.startsWith("name=")) {
                        String query = lowerArg.substring(5);
                        String displayName = (meta != null && meta.hasDisplayName()) ? meta.getDisplayName() : "";
                        if (!ChatColor.stripColor(displayName).toLowerCase().contains(query)) {
                            isMatch = false;
                        }
                    } else if (lowerArg.startsWith("lore=")) {
                        String query = lowerArg.substring(5);
                        boolean loreHit = false;
                        if (meta != null && meta.hasLore()) {
                            for (String line : meta.getLore()) {
                                if (ChatColor.stripColor(line).toLowerCase().contains(query)) {
                                    loreHit = true;
                                    break;
                                }
                            }
                        }
                        if (!loreHit) isMatch = false;
                    } else if (lowerArg.startsWith("model=") || lowerArg.startsWith("data=")) {
                        try {
                            int queryVal = Integer.parseInt(lowerArg.substring(6));
                            if (meta == null || !meta.hasCustomModelData() || meta.getCustomModelData() != queryVal) {
                                isMatch = false;
                            }
                        } catch (NumberFormatException e) {
                            isMatch = false;
                        }
                    } else if (lowerArg.startsWith("material=") || lowerArg.startsWith("type=")) {
                        String prefix = lowerArg.startsWith("material=") ? "material=" : "type=";
                        String query = lowerArg.substring(prefix.length());
                        if (!stack.getType().name().toLowerCase().contains(query)) {
                            isMatch = false;
                        }
                    } else if (lowerArg.startsWith("enchant=")) {
                        String query = lowerArg.substring(8);
                        boolean enchantHit = false;
                        for (Enchantment ench : stack.getEnchantments().keySet()) {
                            if (ench.getName().toLowerCase().contains(query)) {
                                enchantHit = true;
                                break;
                            }
                        }
                        if (!enchantHit) isMatch = false;
                    } else {
                        String displayName = (meta != null && meta.hasDisplayName()) ? meta.getDisplayName() : "";
                        if (!ChatColor.stripColor(displayName).toLowerCase().contains(lowerArg)) {
                            isMatch = false;
                        }
                    }
                    if (!isMatch) break;
                }
                if (isMatch) {
                    matchedItems.add(mmItem);
                }
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                sendResult(player, matchedItems);
            });
        });
    }

    private void sendResult(Player player, List<MythicItem> items) {
        if (items.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "条件に一致するアイテムは見つかりませんでした");
        } else {
            player.sendMessage(ChatColor.GREEN + "=== 検索結果 (" + items.size() + "件) ===");
            int count = 0;
            for (MythicItem item : items) {
                String mmid = item.getInternalName();
                String displayName = item.getDisplayName();
                if (displayName == null) displayName = mmid;
                TextComponent message = new TextComponent(ChatColor.GOLD + "- " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', displayName));
                ComponentBuilder hoverText = new ComponentBuilder(ChatColor.YELLOW + "ID: " + mmid + "\n");
                hoverText.append(ChatColor.GRAY + "クリックでアイテムを入手");
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.create()));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mm items give " + player.getName() + " " + mmid));
                player.spigot().sendMessage(message);
                count++;
            }
        }
    }

    @Override
    public @NotNull List<String> suggest(Player player, String[] args) {
        String currentArg = args[args.length - 1].toLowerCase();
        List<String> suggestions = new ArrayList<>();
        for (String key : KEYS) {
            if (key.startsWith(currentArg)) {
                suggestions.add(key);
            }
        }
        if (currentArg.startsWith("material=") || currentArg.startsWith("type=")) {
            String prefix = currentArg.startsWith("material=") ? "material=" : "type=";
            String val = currentArg.substring(prefix.length());
            return Arrays.stream(Material.values())
                    .map(mat -> prefix + mat.name().toLowerCase())
                    .filter(s -> s.startsWith(prefix + val))
                    .limit(50)
                    .collect(Collectors.toList());
        } else if (currentArg.startsWith("enchant=")) {
            String val = currentArg.substring(8);
            return Arrays.stream(Enchantment.values())
                    .map(ench -> "enchant=" + ench.getName().toLowerCase())
                    .filter(s -> s.startsWith("enchant=" + val))
                    .collect(Collectors.toList());
        }
        return suggestions;
    }
}