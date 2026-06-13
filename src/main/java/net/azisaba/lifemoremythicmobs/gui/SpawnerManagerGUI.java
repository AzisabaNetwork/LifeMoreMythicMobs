package net.azisaba.lifemoremythicmobs.gui;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.spawning.spawners.MythicSpawner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class SpawnerManagerGUI {

    public static final String MAIN_TITLE = "スポナー管理 - カテゴリ選択";
    public static final String WORLD_LIST_TITLE = "スポナー管理 - ワールド別";
    public static final String GROUP_LIST_TITLE = "スポナー管理 - グループ別";
    public static final String SPAWNER_LIST_TITLE_PREFIX = "一覧: ";
    public static final String DETAIL_TITLE_PREFIX = "詳細: ";

    public static void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, MAIN_TITLE);

        inv.setItem(2, createItem(Material.GRASS_BLOCK, ChatColor.GREEN + "ワールド別に表示", ChatColor.GRAY + "ワールドごとにスポナーを表示します"));
        inv.setItem(6, createItem(Material.CHEST, ChatColor.GOLD + "グループ別に表示", ChatColor.GRAY + "グループごとにスポナーを表示します"));

        player.openInventory(inv);
    }

    public static void openWorldList(Player player, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, WORLD_LIST_TITLE);

        List<World> worlds = Bukkit.getWorlds();
        int start = page * 45;
        for (int i = 0; i < 45 && (start + i) < worlds.size(); i++) {
            World world = worlds.get(start + i);
            long count = MythicMobs.inst().getSpawnerManager().getSpawners().stream()
                    .filter(s -> {
                        String w = s.getLocation().getWorld().getName();
                        return w != null && w.equalsIgnoreCase(world.getName());
                    })
                    .count();

            inv.setItem(i, createItem(Material.MAP, ChatColor.AQUA + world.getName(), 
                    ChatColor.YELLOW + "スポナー数: " + ChatColor.WHITE + count,
                    ChatColor.GRAY + "クリックで一覧を表示"));
        }

        if (page > 0) {
            inv.setItem(45, createItem(Material.ARROW, ChatColor.YELLOW + "前のページ"));
        }
        if (worlds.size() > start + 45) {
            inv.setItem(53, createItem(Material.ARROW, ChatColor.YELLOW + "次のページ"));
        }
        inv.setItem(49, createItem(Material.BARRIER, ChatColor.RED + "戻る"));

        player.openInventory(inv);
    }

    public static void openGroupList(Player player, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, GROUP_LIST_TITLE);

        Set<String> groups = MythicMobs.inst().getSpawnerManager().getSpawners().stream()
                .map(MythicSpawner::getGroup)
                .filter(g -> g != null && !g.isEmpty())
                .collect(Collectors.toSet());

        List<String> sortedGroups = new ArrayList<>(groups);
        Collections.sort(sortedGroups);

        int start = page * 45;
        for (int i = 0; i < 45 && (start + i) < sortedGroups.size(); i++) {
            String group = sortedGroups.get(start + i);
            long count = MythicMobs.inst().getSpawnerManager().getSpawners().stream()
                    .filter(s -> group.equalsIgnoreCase(s.getGroup()))
                    .count();

            inv.setItem(i, createItem(Material.CHEST, ChatColor.GOLD + group,
                    ChatColor.YELLOW + "スポナー数: " + ChatColor.WHITE + count,
                    ChatColor.GRAY + "クリックで一覧を表示"));
        }

        if (page > 0) {
            inv.setItem(45, createItem(Material.ARROW, ChatColor.YELLOW + "前のページ"));
        }
        if (sortedGroups.size() > start + 45) {
            inv.setItem(53, createItem(Material.ARROW, ChatColor.YELLOW + "次のページ"));
        }
        inv.setItem(49, createItem(Material.BARRIER, ChatColor.RED + "戻る"));

        player.openInventory(inv);
    }

    public static void openSpawnerList(Player player, String filterType, String filterValue, int page) {
        List<MythicSpawner> spawners;
        if (filterType.equals("world")) {
            spawners = MythicMobs.inst().getSpawnerManager().getSpawners().stream()
                    .filter(s -> {
                        String w = s.getLocation().getWorld().getName();
                        return w != null && w.equalsIgnoreCase(filterValue);
                    })
                    .sorted(Comparator.comparing(MythicSpawner::getInternalName))
                    .collect(Collectors.toList());
        } else {
            spawners = MythicMobs.inst().getSpawnerManager().getSpawners().stream()
                    .filter(s -> {
                        String g = s.getGroup();
                        return g != null && g.equalsIgnoreCase(filterValue);
                    })
                    .sorted(Comparator.comparing(MythicSpawner::getInternalName))
                    .collect(Collectors.toList());
        }

        Inventory inv = Bukkit.createInventory(null, 54, SPAWNER_LIST_TITLE_PREFIX + filterValue);

        int start = page * 45;
        for (int i = 0; i < 45 && (start + i) < spawners.size(); i++) {
            MythicSpawner s = spawners.get(start + i);
            inv.setItem(i, createSpawnerItem(s));
        }

        if (page > 0) {
            inv.setItem(45, createItem(Material.ARROW, ChatColor.YELLOW + "前のページ"));
        }
        if (spawners.size() > start + 45) {
            inv.setItem(53, createItem(Material.ARROW, ChatColor.YELLOW + "次のページ"));
        }
        inv.setItem(49, createItem(Material.BARRIER, ChatColor.RED + "戻る"));

        player.openInventory(inv);
    }

    public static void openDetail(Player player, String spawnerName) {
        MythicSpawner s = MythicMobs.inst().getSpawnerManager().getSpawnerByName(spawnerName);
        if (s == null) {
            player.sendMessage(ChatColor.RED + "スポナーが見つかりませんでした。");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, DETAIL_TITLE_PREFIX + spawnerName);

        inv.setItem(4, createSpawnerItem(s));
        inv.setItem(11, createItem(Material.ENDER_PEARL, ChatColor.AQUA + "テレポート", ChatColor.GRAY + "スポナーの座標にテレポートします"));
        inv.setItem(13, createItem(Material.BEACON, ChatColor.YELLOW + "位置を可視化", ChatColor.GRAY + "パーティクルで場所を表示します"));
        inv.setItem(15, createItem(Material.BARRIER, ChatColor.RED + "削除", ChatColor.GRAY + "このスポナーを削除します"));
        inv.setItem(22, createItem(Material.IRON_DOOR, ChatColor.GRAY + "戻る"));

        player.openInventory(inv);
    }

    private static ItemStack createSpawnerItem(MythicSpawner s) {
        ItemStack item = new ItemStack(Material.SPAWNER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + s.getInternalName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Mob: " + ChatColor.WHITE + s.getTypeName());
        lore.add(ChatColor.YELLOW + "Group: " + ChatColor.WHITE + (s.getGroup() == null ? "なし" : s.getGroup()));
        lore.add(ChatColor.YELLOW + "World: " + ChatColor.WHITE + s.getLocation().getWorld().getName());
        lore.add(ChatColor.YELLOW + "Location: " + ChatColor.WHITE + String.format("%.0f, %.0f, %.0f", s.getLocation().getX(), s.getLocation().getY(), s.getLocation().getZ()));
        lore.add("");
        lore.add(ChatColor.GRAY + "--- 設定 ---");
        lore.add(ChatColor.GRAY + "MaxMobs: " + s.getMaxMobs());
        lore.add(ChatColor.GRAY + "MobLevel: " + s.getMobLevel());
        lore.add(ChatColor.GRAY + "Cooldown: " + s.getCooldownSeconds());
        lore.add(ChatColor.GRAY + "ActivationRange: " + s.getActivationRange());
        lore.add("");
        lore.add(ChatColor.WHITE + "左クリック: 詳細表示");
        lore.add(ChatColor.WHITE + "右クリック: 位置を可視化");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }
}
