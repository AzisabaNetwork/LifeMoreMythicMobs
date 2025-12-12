package net.azisaba.lifemoremythicmobs.commands;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractItemStack;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitPlayer;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FindMythicItemCommand extends SubCommand {

    private static final List<String> keys = Arrays.asList("name=", "lore=", "model=", "data=", "material=", "type=", "enchant=");

    @Override
    public @NotNull String getName() {
        return "findMythicItem";
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "使用法: /lmmm findMythicItem <name=アイテム名 model=モデル番号 material=アイテムID enchant=エンチャント>");
            return;
        }
        List<MythicItem> matchedItems = new ArrayList<>();
        BukkitPlayer mythicPlayer = new BukkitPlayer(player);
        for (MythicItem item : MythicMobs.inst().getItemManager().getItems()) {
            ItemStack stack;
            try {
                AbstractItemStack abstractItemStack = item.generateItemStack(1, mythicPlayer, null);
                if (abstractItemStack == null) continue;
                stack = BukkitAdapter.adapt(abstractItemStack);
            } catch (Exception e) {
                continue;
            }
            if (stack == null) continue;
            boolean isMatch = true;
            for (String arg : args) {
                String lowerArg = arg.toLowerCase();
                if (lowerArg.startsWith("name=")) {
                    String val = lowerArg.substring(5);
                    if (!checkNameMatch(stack, val)) isMatch = false;
                } else if (lowerArg.startsWith("lore=")) {
                    String val = lowerArg.substring(5);
                    if (!checkLoreMatch(stack, val)) isMatch = false;
                } else if (lowerArg.startsWith("model=") || lowerArg.startsWith("data=")) {
                    int prefixLen = lowerArg.startsWith("model=") ? 6 : 5;
                    try {
                        int val = Integer.parseInt(lowerArg.substring(prefixLen));
                        if (!stack.hasItemMeta() || !stack.getItemMeta().hasCustomModelData()
                                || stack.getItemMeta().getCustomModelData() != val) {
                            isMatch = false;
                        }
                    } catch (NumberFormatException e) {
                        isMatch = false;
                    }
                } else if (lowerArg.startsWith("material=") || lowerArg.startsWith("type=")) {
                    int prefixLen = lowerArg.startsWith("material=") ? 9 : 5;
                    String val = lowerArg.substring(prefixLen);
                    if (!stack.getType().name().toLowerCase().contains(val)) isMatch = false;
                } else if (lowerArg.startsWith("enchant=")) {
                    String val = lowerArg.substring(8);
                    boolean hasEnchant = false;
                    for (Map.Entry<Enchantment, Integer> entry : stack.getEnchantments().entrySet()) {
                        if (entry.getKey().getName().toLowerCase().contains(val)) {
                            hasEnchant = true;
                            break;
                        }
                    }
                    if (!hasEnchant) isMatch = false;
                } else {
                    if (!checkNameMatch(stack, lowerArg)) isMatch = false;
                }
                if (!isMatch) break;
            }
            if (isMatch) {
                matchedItems.add(item);
            }
        }
        sendResult(player, matchedItems);
    }

    private boolean checkNameMatch(ItemStack is, String query) {
        String displayName = is.hasItemMeta() && is.getItemMeta().hasDisplayName() ? is.getItemMeta().getDisplayName() : "";
        String stripped = ChatColor.stripColor(displayName).toLowerCase();
        return stripped.contains(query);
    }

    private boolean checkLoreMatch(ItemStack is, String query) {
        if (!is.hasItemMeta() || !is.getItemMeta().hasLore()) return false;
        List<String> lore = is.getItemMeta().getLore();
        if (lore == null) return false;

        for (String line : lore) {
            String stripped = ChatColor.stripColor(line).toLowerCase();
            if (stripped.contains(query)) return true;
        }
        return false;
    }

    private void sendResult(Player player, List<MythicItem> items) {
        if (items.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "該当するアイテムが見つかりませんでした。");
        } else {
            player.sendMessage(ChatColor.GREEN + "=== 検索結果 (" + items.size() + "件) ===");
            for (MythicItem item : items) {
                String mmid = item.getInternalName();
                String displayName = item.getDisplayName() != null ? item.getDisplayName() : mmid;
                String command = "/mm items give " + player.getName() + " " + mmid;
                ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("クリックで入手").create());
                TextComponent root = new TextComponent(mmid);
                root.setClickEvent(clickEvent);
                root.setHoverEvent(hoverEvent);
                for (BaseComponent component : TextComponent.fromLegacyText(ChatColor.WHITE + " -> " + displayName)) {
                    root.addExtra(component);
                }
                player.spigot().sendMessage(root);
            }
        }
    }

    @Override
    public @NotNull List<@NotNull String> suggest(@NotNull Player player, @NotNull String @NotNull [] args) {
        String currentArg = args[args.length - 1].toLowerCase();
        List<String> suggestions = new ArrayList<>();
        List<String> usedKeys = new ArrayList<>();
        for (int i = 0; i < args.length - 1; i++) {
            String arg = args[i].toLowerCase();
            for (String key : keys) {
                if (arg.startsWith(key)) {
                    if (key.equals("model=") || key.equals("data=")) {
                        usedKeys.add("model="); usedKeys.add("data=");
                    } else if (key.equals("material=") || key.equals("type=")) {
                        usedKeys.add("material="); usedKeys.add("type=");
                    } else {
                        usedKeys.add(key);
                    }
                }
            }
        }
        for (String key : keys) {
            if (!usedKeys.contains(key) && key.startsWith(currentArg)) {
                suggestions.add(key);
            }
        }
        if (currentArg.startsWith("material=") || currentArg.startsWith("type=")) {
            String prefix = currentArg.startsWith("material=") ? "material=" : "type=";
            String val = currentArg.substring(prefix.length());
            suggestions.addAll(Arrays.stream(Material.values())
                    .map(mat -> prefix + mat.name().toLowerCase())
                    .filter(s -> s.startsWith(prefix + val))
                    .collect(Collectors.toList()));
        }
        else if (currentArg.startsWith("enchant=")) {
            String val = currentArg.substring(8);
            suggestions.addAll(Arrays.stream(Enchantment.values())
                    .map(ench -> "enchant=" + ench.getName().toLowerCase())
                    .filter(s -> s.startsWith("enchant=" + val))
                    .collect(Collectors.toList()));
        }
        return suggestions;
    }
}
