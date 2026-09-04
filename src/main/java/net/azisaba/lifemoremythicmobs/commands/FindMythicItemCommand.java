package net.azisaba.lifemoremythicmobs.commands;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.items.MythicItem;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.azisaba.lifemoremythicmobs.util.LegacyText;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
    public @NotNull String getName() {
        return "findMythicItem";
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String[] args) {
        if (args.length == 0) {
            player.sendMessage(LegacyText.RED + "使用法: /lmmm findMythicItem <name=アイテム名 | model=モデル番号 | material=アイテムID | enchant=エンチャント名>");
            return;
        }
        player.sendMessage(LegacyText.YELLOW + "アイテムを検索しています...");
        // MythicItem generation and Bukkit ItemMeta access are not thread-safe.
        // Keep the scan on the server thread; running this asynchronously caused API calls and
        // MythicMobs warnings to originate from a Craft Scheduler worker.
        Bukkit.getScheduler().runTask(plugin, () -> {
            List<MythicItem> matchedItems = new ArrayList<>();
            Collection<MythicItem> allItems = MythicBukkit.inst().getItemManager().getItems();
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
                        String displayName = meta != null && meta.displayName() != null ? LegacyText.plain(meta.displayName()) : "";
                        if (!LegacyText.stripColor(displayName).toLowerCase().contains(query)) {
                            isMatch = false;
                        }
                    } else if (lowerArg.startsWith("lore=")) {
                        String query = lowerArg.substring(5);
                        boolean loreHit = false;
                        if (meta != null && meta.lore() != null) {
                            for (Component line : meta.lore()) {
                                if (LegacyText.plain(line).toLowerCase().contains(query)) {
                                    loreHit = true;
                                    break;
                                }
                            }
                        }
                        if (!loreHit) isMatch = false;
                    } else if (lowerArg.startsWith("model=") || lowerArg.startsWith("data=")) {
                        try {
                            int queryVal = Integer.parseInt(lowerArg.substring(6));
                            if (meta == null || meta.getCustomModelDataComponent().getFloats().isEmpty()
                                    || meta.getCustomModelDataComponent().getFloats().getFirst().intValue() != queryVal) {
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
                            if (ench.getKey().getKey().toLowerCase().contains(query)) {
                                enchantHit = true;
                                break;
                            }
                        }
                        if (!enchantHit) isMatch = false;
                    } else {
                        String displayName = meta != null && meta.displayName() != null ? LegacyText.plain(meta.displayName()) : "";
                        if (!LegacyText.stripColor(displayName).toLowerCase().contains(lowerArg)) {
                            isMatch = false;
                        }
                    }
                    if (!isMatch) break;
                }
                if (isMatch) {
                    matchedItems.add(mmItem);
                }
            }
            sendResult(player, matchedItems);
        });
    }

    private void sendResult(Player player, List<MythicItem> items) {
        if (items.isEmpty()) {
            player.sendMessage(LegacyText.YELLOW + "条件に一致するアイテムは見つかりませんでした");
        } else {
            player.sendMessage(LegacyText.GREEN + "=== 検索結果 (" + items.size() + "件) ===");
            for (MythicItem item : items) {
                String mmid = item.getInternalName();
                String displayName = item.getDisplayName();
                if (displayName == null) displayName = mmid;
                Component message = LegacyText.component(LegacyText.GOLD + "- " + LegacyText.RESET)
                        .append(LegacyText.ampersandComponent(displayName))
                        .hoverEvent(HoverEvent.showText(LegacyText.component(LegacyText.YELLOW + "ID: " + mmid + "\n" + LegacyText.GRAY + "クリックでアイテムを入手")))
                        .clickEvent(ClickEvent.suggestCommand("/mm items give " + player.getName() + " " + mmid));
                player.sendMessage(message);
            }
        }
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
            return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).stream()
                    .map(ench -> "enchant=" + ench.getKey().getKey().toLowerCase())
                    .filter(s -> s.startsWith("enchant=" + val))
                    .collect(Collectors.toList());
        }
        return suggestions;
    }
}
