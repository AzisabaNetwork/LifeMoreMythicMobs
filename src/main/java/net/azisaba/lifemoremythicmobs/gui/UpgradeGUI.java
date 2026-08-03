package net.azisaba.lifemoremythicmobs.gui;

import net.azisaba.lifemoremythicmobs.upgrade.UpgradeStatManager;
import net.azisaba.lifemoremythicmobs.upgrade.UpgradeStatManager.StatType;
import org.bukkit.Bukkit;
import net.azisaba.lifemoremythicmobs.util.LegacyText;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class UpgradeGUI {

    public static final String TITLE_PREFIX = "能力強化: ";
    public static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

    public static void open(Player player, String profile) {
        Inventory inv = Bukkit.createInventory(null, 54, LegacyText.component(TITLE_PREFIX + profile));

        List<StatType> types = getDisplayedTypes(profile);

        for (int i = 0; i < types.size() && i < SLOTS.length; i++) {
            inv.setItem(SLOTS[i], createStatItem(player, profile, types.get(i)));
        }

        inv.setItem(49, createItem(Material.BARRIER, LegacyText.RED + "閉じる"));

        player.openInventory(inv);
    }

    public static List<StatType> getDisplayedTypes(String profile) {
        List<StatType> types = new ArrayList<>();

        if (profile.equalsIgnoreCase("hw2026")) {
            types.add(StatType.FIRE_DMG);
            types.add(StatType.FIRE_RES);
            types.add(StatType.WATER_DMG);
            types.add(StatType.WATER_RES);
            types.add(StatType.LEAF_DMG);
            types.add(StatType.LEAF_RES);
        }
        return types;
    }

    private static ItemStack createStatItem(Player player, String profile, StatType type) {
        int level = UpgradeStatManager.getLevel(player, profile, type);
        boolean isMax = level >= UpgradeStatManager.MAX_LEVEL;
        int cost = (level + 1) * 5;

        Material mat = Material.BOOK;
        switch (type) {
            case FIRE_DMG: mat = Material.FIRE_CHARGE; break;
            case FIRE_RES: mat = Material.MAGMA_CREAM; break;
            case WATER_DMG: mat = Material.WATER_BUCKET; break;
            case WATER_RES: mat = Material.PRISMARINE_SHARD; break;
            case LEAF_DMG: mat = Material.OAK_LEAVES; break;
            case LEAF_RES: mat = Material.GRASS_BLOCK; break;
        }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(LegacyText.component(LegacyText.GOLD + type.displayName + (isMax ? LegacyText.RED + " [MAX]" : "")));

        List<String> lore = new ArrayList<>();
        lore.add(LegacyText.YELLOW + "現在のレベル: " + LegacyText.WHITE + level + (isMax ? LegacyText.RED + " (最大)" : ""));

        String effectValue = "+" + (int)(level * type.perLevel * 100) + "%";

        lore.add(LegacyText.YELLOW + "効果: " + LegacyText.WHITE + effectValue);
        lore.add("");
        if (!isMax) {
            lore.add(LegacyText.AQUA + "次へのコスト: " + LegacyText.GREEN + cost + " ポイント");
            lore.add("");
        }
        lore.add(LegacyText.WHITE + "左クリックして強化");
        lore.add(LegacyText.WHITE + "右クリックして強化を取り消し");

        meta.lore(LegacyText.components(lore));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(LegacyText.component(name));
        item.setItemMeta(meta);
        return item;
    }
}