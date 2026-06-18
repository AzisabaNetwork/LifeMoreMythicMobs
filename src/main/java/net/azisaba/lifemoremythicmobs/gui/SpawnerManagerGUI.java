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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.util.*;
import java.util.stream.Collectors;

public class SpawnerManagerGUI {

    public static final String MAIN_TITLE = "スポナー管理 - カテゴリ選択";
    public static final String WORLD_LIST_TITLE = "スポナー管理 - ワールド別";
    public static final String GROUP_LIST_TITLE = "スポナー管理 - グループ別";
    public static final String SPAWNER_LIST_TITLE_PREFIX = "一覧: ";
    public static final String DETAIL_TITLE_PREFIX = "詳細: ";
    public static final String GROUP_EDIT_TITLE_PREFIX = "一括編集: ";
    public static final String INDIVIDUAL_EDIT_TITLE_PREFIX = "個別編集: ";
    public static final String SELECT_EDIT_TITLE = "一括編集(選択)";
    public static final String CONFIRM_DELETE_SELECTED_TITLE = "選択削除の確認";

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
        
        if (filterType.equals("group")) {
            inv.setItem(47, createItem(Material.WRITABLE_BOOK, ChatColor.YELLOW + "グループ一括編集", ChatColor.GRAY + "このグループの全スポナーを編集します"));
        }

        // New controls for arbitrary multi-select
        inv.setItem(46, createItem(Material.TORCH, ChatColor.AQUA + "選択モード切替", ChatColor.GRAY + "クリックでON/OFF"));
        inv.setItem(48, createItem(Material.BOOK, ChatColor.YELLOW + "選択一括編集", ChatColor.GRAY + "選択中のスポナーを編集します"));
        inv.setItem(50, createItem(Material.LAVA_BUCKET, ChatColor.RED + "選択一括削除", ChatColor.GRAY + "選択中のスポナーを削除します"));

        inv.setItem(49, createItem(Material.BARRIER, ChatColor.RED + "戻る"));

        player.openInventory(inv);
    }

    public static void openSpawnerList(Player player, String filterType, String filterValue, int page, java.util.Set<String> selected, boolean selectionMode) {
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
            boolean isSelected = selected != null && selected.contains(s.getInternalName());
            inv.setItem(i, createSpawnerItemWithSelectState(s, isSelected, selectionMode));
        }

        if (page > 0) {
            inv.setItem(45, createItem(Material.ARROW, ChatColor.YELLOW + "前のページ"));
        }
        if (spawners.size() > start + 45) {
            inv.setItem(53, createItem(Material.ARROW, ChatColor.YELLOW + "次のページ"));
        }

        if (filterType.equals("group")) {
            inv.setItem(47, createItem(Material.WRITABLE_BOOK, ChatColor.YELLOW + "グループ一括編集", ChatColor.GRAY + "このグループの全スポナーを編集します"));
        }

        String mode = selectionMode ? (ChatColor.GREEN + "ON") : (ChatColor.RED + "OFF");
        inv.setItem(46, createItem(Material.TORCH, ChatColor.AQUA + "選択モード: " + mode, ChatColor.GRAY + "クリックでON/OFF"));
        int selectedCount = selected == null ? 0 : selected.size();
        inv.setItem(48, createItem(Material.BOOK, ChatColor.YELLOW + "選択一括編集", ChatColor.GRAY + "選択数: " + ChatColor.WHITE + selectedCount));
        inv.setItem(50, createItem(Material.LAVA_BUCKET, ChatColor.RED + "選択一括削除", ChatColor.GRAY + "選択数: " + ChatColor.WHITE + selectedCount));

        inv.setItem(49, createItem(Material.BARRIER, ChatColor.RED + "戻る"));

        player.openInventory(inv);
    }

    public static void openGroupEdit(Player player, String groupName) {
        List<MythicSpawner> spawners = MythicMobs.inst().getSpawnerManager().getSpawners().stream()
                .filter(s -> groupName.equalsIgnoreCase(s.getGroup()))
                .collect(Collectors.toList());

        MythicSpawner representative = spawners.isEmpty() ? null : spawners.get(0);

        Inventory inv = Bukkit.createInventory(null, 27, GROUP_EDIT_TITLE_PREFIX + groupName);

        String currentMob = representative != null ? representative.getTypeName() : "???";
        inv.setItem(0, createItem(Material.ZOMBIE_SPAWN_EGG, ChatColor.YELLOW + "モブ名の変更", 
                ChatColor.GRAY + "代表値: " + ChatColor.WHITE + currentMob,
                ChatColor.GRAY + "全スポナーの出現モブ(MobType)を変更します"));
        
        // Row 2: Basic options
        String maxMobs = representative != null ? String.valueOf(representative.getMaxMobs()) : "???";
        inv.setItem(9, createItem(Material.IRON_INGOT, ChatColor.YELLOW + "MaxMobs の変更", 
                ChatColor.GRAY + "代表値: " + ChatColor.WHITE + maxMobs,
                ChatColor.GRAY + "一括で変更します"));

        String mobLevel = representative != null ? String.valueOf(representative.getMobLevel()) : "???";
        inv.setItem(10, createItem(Material.EXPERIENCE_BOTTLE, ChatColor.YELLOW + "MobLevel の変更", 
                ChatColor.GRAY + "代表値: " + ChatColor.WHITE + mobLevel,
                ChatColor.GRAY + "一括で変更します"));

        String radius = representative != null ? String.valueOf(representative.getSpawnRadius()) : "???";
        inv.setItem(11, createItem(Material.COMPASS, ChatColor.YELLOW + "Radius の変更", 
                ChatColor.GRAY + "代表値: " + ChatColor.WHITE + radius,
                ChatColor.GRAY + "一括で変更します"));

        String activationRange = representative != null ? String.valueOf(representative.getActivationRange()) : "???";
        inv.setItem(12, createItem(Material.BEACON, ChatColor.YELLOW + "ActivationRange の変更", 
                ChatColor.GRAY + "代表値: " + ChatColor.WHITE + activationRange,
                ChatColor.GRAY + "一括で変更します"));

        String leashRange = representative != null ? String.valueOf(representative.getLeashRange()) : "???";
        inv.setItem(13, createItem(Material.LEAD, ChatColor.YELLOW + "LeashRange の変更", 
                ChatColor.GRAY + "代表値: " + ChatColor.WHITE + leashRange,
                ChatColor.GRAY + "一括で変更します"));

        String cooldown = representative != null ? String.valueOf(representative.getCooldownSeconds()) : "???";
        inv.setItem(14, createItem(Material.CLOCK, ChatColor.YELLOW + "Cooldown の変更", 
                ChatColor.GRAY + "代表値: " + ChatColor.WHITE + cooldown,
                ChatColor.GRAY + "一括で変更します"));

        String warmup = representative != null ? String.valueOf(representative.getWarmupSeconds()) : "???";
        inv.setItem(15, createItem(Material.CAMPFIRE, ChatColor.YELLOW + "Warmup の変更", 
                ChatColor.GRAY + "代表値: " + ChatColor.WHITE + warmup,
                ChatColor.GRAY + "一括で変更します"));

        String amountValue = "???";
        if (representative != null) {
            try {
                java.lang.reflect.Method m = representative.getClass().getMethod("getMobsPerSpawn");
                amountValue = String.valueOf(m.invoke(representative));
            } catch (Exception ignored) {}
        }
        inv.setItem(16, createItem(Material.SLIME_BALL, ChatColor.YELLOW + "Amount の変更", 
                ChatColor.GRAY + "代表値: " + ChatColor.WHITE + amountValue,
                ChatColor.GRAY + "一括で変更します"));

        inv.setItem(22, createItem(Material.IRON_DOOR, ChatColor.GRAY + "戻る"));
        
        player.openInventory(inv);
    }

    public static void openIndividualEdit(Player player, String spawnerName) {
        MythicSpawner s = MythicMobs.inst().getSpawnerManager().getSpawnerByName(spawnerName);
        if (s == null) {
            player.sendMessage(ChatColor.RED + "スポナーが見つかりませんでした。");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, INDIVIDUAL_EDIT_TITLE_PREFIX + spawnerName);

        inv.setItem(0, createItem(Material.ZOMBIE_SPAWN_EGG, ChatColor.YELLOW + "モブ名の変更", 
                ChatColor.GRAY + "現在の値: " + ChatColor.WHITE + s.getTypeName(),
                ChatColor.GRAY + "このスポナーの出現モブ(MobType)を変更します",
                "", ChatColor.AQUA + "右クリックで元に戻す"));

        // Row 2: Basic options
        inv.setItem(9, createItem(Material.IRON_INGOT, ChatColor.YELLOW + "MaxMobs の変更", 
                ChatColor.GRAY + "現在の値: " + ChatColor.WHITE + s.getMaxMobs(),
                ChatColor.GRAY + "設定値を変更します",
                "", ChatColor.AQUA + "右クリックで元に戻す"));
        inv.setItem(10, createItem(Material.EXPERIENCE_BOTTLE, ChatColor.YELLOW + "MobLevel の変更", 
                ChatColor.GRAY + "現在の値: " + ChatColor.WHITE + s.getMobLevel(),
                ChatColor.GRAY + "設定値を変更します",
                "", ChatColor.AQUA + "右クリックで元に戻す"));
        inv.setItem(11, createItem(Material.COMPASS, ChatColor.YELLOW + "Radius の変更", 
                ChatColor.GRAY + "現在の値: " + ChatColor.WHITE + s.getSpawnRadius(),
                ChatColor.GRAY + "設定値を変更します",
                "", ChatColor.AQUA + "右クリックで元に戻す"));
        inv.setItem(12, createItem(Material.BEACON, ChatColor.YELLOW + "ActivationRange の変更", 
                ChatColor.GRAY + "現在の値: " + ChatColor.WHITE + s.getActivationRange(),
                ChatColor.GRAY + "設定値を変更します",
                "", ChatColor.AQUA + "右クリックで元に戻す"));
        inv.setItem(13, createItem(Material.LEAD, ChatColor.YELLOW + "LeashRange の変更", 
                ChatColor.GRAY + "現在の値: " + ChatColor.WHITE + s.getLeashRange(),
                ChatColor.GRAY + "設定値を変更します",
                "", ChatColor.AQUA + "右クリックで元に戻す"));
        inv.setItem(14, createItem(Material.CLOCK, ChatColor.YELLOW + "Cooldown の変更", 
                ChatColor.GRAY + "現在の値: " + ChatColor.WHITE + s.getCooldownSeconds(),
                ChatColor.GRAY + "設定値を変更します",
                "", ChatColor.AQUA + "右クリックで元に戻す"));
        inv.setItem(15, createItem(Material.CAMPFIRE, ChatColor.YELLOW + "Warmup の変更", 
                ChatColor.GRAY + "現在の値: " + ChatColor.WHITE + s.getWarmupSeconds(),
                ChatColor.GRAY + "設定値を変更します",
                "", ChatColor.AQUA + "右クリックで元に戻す"));
        
        String amount = "???";
        try {
            java.lang.reflect.Method m = s.getClass().getMethod("getMobsPerSpawn");
            amount = String.valueOf(m.invoke(s));
        } catch (Exception ignored) {}

        inv.setItem(16, createItem(Material.SLIME_BALL, ChatColor.YELLOW + "Amount の変更", 
                ChatColor.GRAY + "現在の値: " + ChatColor.WHITE + amount,
                ChatColor.GRAY + "設定値を変更します",
                "", ChatColor.AQUA + "右クリックで元に戻す"));

        inv.setItem(22, createItem(Material.IRON_DOOR, ChatColor.GRAY + "戻る"));

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
        inv.setItem(10, createItem(Material.WRITABLE_BOOK, ChatColor.YELLOW + "編集", ChatColor.GRAY + "このスポナーの設定を変更します"));
        inv.setItem(12, createItem(Material.ENDER_PEARL, ChatColor.AQUA + "テレポート", ChatColor.GRAY + "スポナーの座標にテレポートします"));
        inv.setItem(14, createItem(Material.BEACON, ChatColor.YELLOW + "位置を可視化", ChatColor.GRAY + "パーティクルで場所を表示します"));
        inv.setItem(16, createItem(Material.BARRIER, ChatColor.RED + "削除", ChatColor.GRAY + "このスポナーを削除します"));
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
        lore.add(ChatColor.GRAY + "Warmup: " + s.getWarmupSeconds());
        lore.add(ChatColor.GRAY + "Radius: " + s.getSpawnRadius());
        lore.add(ChatColor.GRAY + "ActivationRange: " + s.getActivationRange());
        lore.add(ChatColor.GRAY + "LeashRange: " + s.getLeashRange());

        String amount = "???";
        try {
            java.lang.reflect.Method m = s.getClass().getMethod("getMobsPerSpawn");
            amount = String.valueOf(m.invoke(s));
        } catch (Exception ignored) {}
        lore.add(ChatColor.GRAY + "Amount: " + amount);

        lore.add(ChatColor.GRAY + "CheckForPlayers: " + s.isCheckForPlayers());
        lore.add("");
        lore.add(ChatColor.WHITE + "左クリック: 詳細表示");
        lore.add(ChatColor.WHITE + "シフト左クリック: 編集");
        lore.add(ChatColor.WHITE + "右クリック: 位置を可視化");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createSpawnerItemWithSelectState(MythicSpawner s, boolean selected, boolean selectionMode) {
        ItemStack base = createSpawnerItem(s);
        ItemMeta meta = base.getItemMeta();
        String name = ChatColor.GOLD + s.getInternalName();
        if (selected) {
            name = ChatColor.GREEN + "[選択] " + ChatColor.GOLD + s.getInternalName();
        }
        meta.setDisplayName(name);
        List<String> lore = meta.getLore() == null ? new ArrayList<>() : new ArrayList<>(meta.getLore());
        lore.add(0, selected ? ChatColor.GREEN + "このスポナーは選択されています" : ChatColor.GRAY + "未選択");
        lore.add("");
        if (selectionMode) {
            lore.add(ChatColor.WHITE + "左クリック: 選択/解除");
            lore.add(ChatColor.WHITE + "右クリック: 選択/解除");
        } else {
            lore.add(ChatColor.WHITE + "左クリック: 詳細表示");
            lore.add(ChatColor.WHITE + "シフト左クリック: 編集");
            lore.add(ChatColor.WHITE + "右クリック: 位置を可視化");
        }
        meta.setLore(lore);
        // 視認性向上のため、選択中のスポナーにはエンチャント光を付与
        if (selected) {
            try {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } catch (Throwable ignored) {
                // サーバーバージョン差異などで失敗しても致命的ではないため無視
            }
        } else {
            try {
                // 未選択時は余計な光を消す（他画面から戻ったケースに備える）
                for (ItemFlag flag : ItemFlag.values()) {
                    if (flag == ItemFlag.HIDE_ENCHANTS) meta.removeItemFlags(flag);
                }
                // Enchant は ItemMeta から個別削除 API が無い場合があるため、セットし直しで対応
                // ここでは未選択時に新規付与はしないため何もしない
            } catch (Throwable ignored) {}
        }
        base.setItemMeta(meta);
        return base;
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

    public static void openSelectedEdit(Player player, java.util.Set<String> selectedNames) {
        String title = SELECT_EDIT_TITLE;
        Inventory inv = Bukkit.createInventory(null, 27, title);

        MythicSpawner representative = null;
        for (String name : selectedNames) {
            representative = MythicMobs.inst().getSpawnerManager().getSpawnerByName(name);
            if (representative != null) break;
        }

        String currentMob = representative != null ? representative.getTypeName() : "???";
        inv.setItem(0, createItem(Material.ZOMBIE_SPAWN_EGG, ChatColor.YELLOW + "モブ名の変更",
                ChatColor.GRAY + "代表値: " + ChatColor.WHITE + currentMob,
                ChatColor.GRAY + "選択中のスポナーを一括変更します"));

        String maxMobs = representative != null ? String.valueOf(representative.getMaxMobs()) : "???";
        inv.setItem(9, createItem(Material.IRON_INGOT, ChatColor.YELLOW + "MaxMobs の変更",
                ChatColor.GRAY + "代表値: " + ChatColor.WHITE + maxMobs,
                ChatColor.GRAY + "一括で変更します"));

        String mobLevel = representative != null ? String.valueOf(representative.getMobLevel()) : "???";
        inv.setItem(10, createItem(Material.EXPERIENCE_BOTTLE, ChatColor.YELLOW + "MobLevel の変更",
                ChatColor.GRAY + "代表値: " + ChatColor.WHITE + mobLevel,
                ChatColor.GRAY + "一括で変更します"));

        String radius = representative != null ? String.valueOf(representative.getSpawnRadius()) : "???";
        inv.setItem(11, createItem(Material.COMPASS, ChatColor.YELLOW + "Radius の変更",
                ChatColor.GRAY + "代表値: " + ChatColor.WHITE + radius,
                ChatColor.GRAY + "一括で変更します"));

        String activationRange = representative != null ? String.valueOf(representative.getActivationRange()) : "???";
        inv.setItem(12, createItem(Material.BEACON, ChatColor.YELLOW + "ActivationRange の変更",
                ChatColor.GRAY + "代表値: " + ChatColor.WHITE + activationRange,
                ChatColor.GRAY + "一括で変更します"));

        String leashRange = representative != null ? String.valueOf(representative.getLeashRange()) : "???";
        inv.setItem(13, createItem(Material.LEAD, ChatColor.YELLOW + "LeashRange の変更",
                ChatColor.GRAY + "代表値: " + ChatColor.WHITE + leashRange,
                ChatColor.GRAY + "一括で変更します"));

        String cooldown = representative != null ? String.valueOf(representative.getCooldownSeconds()) : "???";
        inv.setItem(14, createItem(Material.CLOCK, ChatColor.YELLOW + "Cooldown の変更",
                ChatColor.GRAY + "代表値: " + ChatColor.WHITE + cooldown,
                ChatColor.GRAY + "一括で変更します"));

        String warmup = representative != null ? String.valueOf(representative.getWarmupSeconds()) : "???";
        inv.setItem(15, createItem(Material.CAMPFIRE, ChatColor.YELLOW + "Warmup の変更",
                ChatColor.GRAY + "代表値: " + ChatColor.WHITE + warmup,
                ChatColor.GRAY + "一括で変更します"));

        String amountValue = "???";
        if (representative != null) {
            try {
                java.lang.reflect.Method m = representative.getClass().getMethod("getMobsPerSpawn");
                amountValue = String.valueOf(m.invoke(representative));
            } catch (Exception ignored) {}
        }
        inv.setItem(16, createItem(Material.SLIME_BALL, ChatColor.YELLOW + "Amount の変更",
                ChatColor.GRAY + "代表値: " + ChatColor.WHITE + amountValue,
                ChatColor.GRAY + "一括で変更します"));

        inv.setItem(22, createItem(Material.IRON_DOOR, ChatColor.GRAY + "戻る"));

        player.openInventory(inv);
    }

    public static void openConfirmBulkDeleteSelected(Player player, int count) {
        Inventory inv = Bukkit.createInventory(null, 27, CONFIRM_DELETE_SELECTED_TITLE);
        inv.setItem(11, createItem(Material.LIME_WOOL, ChatColor.GREEN + "実行",
                ChatColor.GRAY + "選択された " + ChatColor.WHITE + count + ChatColor.GRAY + " 個を削除します"));
        inv.setItem(15, createItem(Material.RED_WOOL, ChatColor.RED + "キャンセル"));
        inv.setItem(22, createItem(Material.IRON_DOOR, ChatColor.GRAY + "戻る"));
        player.openInventory(inv);
    }
}
