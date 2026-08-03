package net.azisaba.lifemoremythicmobs.gui;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.spawning.spawners.MythicSpawner;
import org.bukkit.Bukkit;
import net.azisaba.lifemoremythicmobs.util.LegacyText;
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
        Inventory inv = Bukkit.createInventory(null, 9, LegacyText.component(MAIN_TITLE));

        inv.setItem(2, createItem(Material.GRASS_BLOCK, LegacyText.GREEN + "ワールド別に表示", LegacyText.GRAY + "ワールドごとにスポナーを表示します"));
        inv.setItem(6, createItem(Material.CHEST, LegacyText.GOLD + "グループ別に表示", LegacyText.GRAY + "グループごとにスポナーを表示します"));

        player.openInventory(inv);
    }

    public static void openWorldList(Player player, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, LegacyText.component(WORLD_LIST_TITLE));

        List<World> worlds = Bukkit.getWorlds();
        int start = page * 45;
        for (int i = 0; i < 45 && (start + i) < worlds.size(); i++) {
            World world = worlds.get(start + i);
            long count = MythicBukkit.inst().getSpawnerManager().getSpawners().stream()
                    .filter(s -> {
                        String w = s.getLocation().getWorld().getName();
                        return w != null && w.equalsIgnoreCase(world.getName());
                    })
                    .count();

            inv.setItem(i, createItem(Material.MAP, LegacyText.AQUA + world.getName(), 
                    LegacyText.YELLOW + "スポナー数: " + LegacyText.WHITE + count,
                    LegacyText.GRAY + "クリックで一覧を表示"));
        }

        if (page > 0) {
            inv.setItem(45, createItem(Material.ARROW, LegacyText.YELLOW + "前のページ"));
        }
        if (worlds.size() > start + 45) {
            inv.setItem(53, createItem(Material.ARROW, LegacyText.YELLOW + "次のページ"));
        }
        inv.setItem(49, createItem(Material.BARRIER, LegacyText.RED + "戻る"));

        player.openInventory(inv);
    }

    public static void openGroupList(Player player, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, LegacyText.component(GROUP_LIST_TITLE));

        Set<String> groups = MythicBukkit.inst().getSpawnerManager().getSpawners().stream()
                .map(MythicSpawner::getGroup)
                .filter(g -> g != null && !g.isEmpty())
                .collect(Collectors.toSet());

        List<String> sortedGroups = new ArrayList<>(groups);
        Collections.sort(sortedGroups);

        int start = page * 45;
        for (int i = 0; i < 45 && (start + i) < sortedGroups.size(); i++) {
            String group = sortedGroups.get(start + i);
            long count = MythicBukkit.inst().getSpawnerManager().getSpawners().stream()
                    .filter(s -> group.equalsIgnoreCase(s.getGroup()))
                    .count();

            inv.setItem(i, createItem(Material.CHEST, LegacyText.GOLD + group,
                    LegacyText.YELLOW + "スポナー数: " + LegacyText.WHITE + count,
                    LegacyText.GRAY + "クリックで一覧を表示"));
        }

        if (page > 0) {
            inv.setItem(45, createItem(Material.ARROW, LegacyText.YELLOW + "前のページ"));
        }
        if (sortedGroups.size() > start + 45) {
            inv.setItem(53, createItem(Material.ARROW, LegacyText.YELLOW + "次のページ"));
        }
        inv.setItem(49, createItem(Material.BARRIER, LegacyText.RED + "戻る"));

        player.openInventory(inv);
    }

    public static void openSpawnerList(Player player, String filterType, String filterValue, int page) {
        List<MythicSpawner> spawners;
        if (filterType.equals("world")) {
            spawners = MythicBukkit.inst().getSpawnerManager().getSpawners().stream()
                    .filter(s -> {
                        String w = s.getLocation().getWorld().getName();
                        return w != null && w.equalsIgnoreCase(filterValue);
                    })
                    .sorted(Comparator.comparing(MythicSpawner::getName))
                    .collect(Collectors.toList());
        } else {
            spawners = MythicBukkit.inst().getSpawnerManager().getSpawners().stream()
                    .filter(s -> {
                        String g = s.getGroup();
                        return g != null && g.equalsIgnoreCase(filterValue);
                    })
                    .sorted(Comparator.comparing(MythicSpawner::getName))
                    .collect(Collectors.toList());
        }

        Inventory inv = Bukkit.createInventory(null, 54, LegacyText.component(SPAWNER_LIST_TITLE_PREFIX + filterValue));

        int start = page * 45;
        for (int i = 0; i < 45 && (start + i) < spawners.size(); i++) {
            MythicSpawner s = spawners.get(start + i);
            inv.setItem(i, createSpawnerItem(s));
        }

        if (page > 0) {
            inv.setItem(45, createItem(Material.ARROW, LegacyText.YELLOW + "前のページ"));
        }
        if (spawners.size() > start + 45) {
            inv.setItem(53, createItem(Material.ARROW, LegacyText.YELLOW + "次のページ"));
        }
        
        if (filterType.equals("group")) {
            inv.setItem(47, createItem(Material.WRITABLE_BOOK, LegacyText.YELLOW + "グループ一括編集", LegacyText.GRAY + "このグループの全スポナーを編集します"));
        }

        // New controls for arbitrary multi-select
        inv.setItem(46, createItem(Material.TORCH, LegacyText.AQUA + "選択モード切替", LegacyText.GRAY + "クリックでON/OFF"));
        inv.setItem(48, createItem(Material.BOOK, LegacyText.YELLOW + "選択一括編集", LegacyText.GRAY + "選択中のスポナーを編集します"));
        inv.setItem(50, createItem(Material.LAVA_BUCKET, LegacyText.RED + "選択一括削除", LegacyText.GRAY + "選択中のスポナーを削除します"));

        inv.setItem(49, createItem(Material.BARRIER, LegacyText.RED + "戻る"));

        player.openInventory(inv);
    }

    public static void openSpawnerList(Player player, String filterType, String filterValue, int page, java.util.Set<String> selected, boolean selectionMode) {
        List<MythicSpawner> spawners;
        if (filterType.equals("world")) {
            spawners = MythicBukkit.inst().getSpawnerManager().getSpawners().stream()
                    .filter(s -> {
                        String w = s.getLocation().getWorld().getName();
                        return w != null && w.equalsIgnoreCase(filterValue);
                    })
                    .sorted(Comparator.comparing(MythicSpawner::getInternalName))
                    .collect(Collectors.toList());
        } else {
            spawners = MythicBukkit.inst().getSpawnerManager().getSpawners().stream()
                    .filter(s -> {
                        String g = s.getGroup();
                        return g != null && g.equalsIgnoreCase(filterValue);
                    })
                    .sorted(Comparator.comparing(MythicSpawner::getInternalName))
                    .collect(Collectors.toList());
        }

        Inventory inv = Bukkit.createInventory(null, 54, LegacyText.component(SPAWNER_LIST_TITLE_PREFIX + filterValue));

        int start = page * 45;
        for (int i = 0; i < 45 && (start + i) < spawners.size(); i++) {
            MythicSpawner s = spawners.get(start + i);
            boolean isSelected = selected != null && selected.contains(s.getInternalName());
            inv.setItem(i, createSpawnerItemWithSelectState(s, isSelected, selectionMode));
        }

        if (page > 0) {
            inv.setItem(45, createItem(Material.ARROW, LegacyText.YELLOW + "前のページ"));
        }
        if (spawners.size() > start + 45) {
            inv.setItem(53, createItem(Material.ARROW, LegacyText.YELLOW + "次のページ"));
        }

        if (filterType.equals("group")) {
            inv.setItem(47, createItem(Material.WRITABLE_BOOK, LegacyText.YELLOW + "グループ一括編集", LegacyText.GRAY + "このグループの全スポナーを編集します"));
        }

        String mode = selectionMode ? (LegacyText.GREEN + "ON") : (LegacyText.RED + "OFF");
        inv.setItem(46, createItem(Material.TORCH, LegacyText.AQUA + "選択モード: " + mode, LegacyText.GRAY + "クリックでON/OFF"));
        int selectedCount = selected == null ? 0 : selected.size();
        inv.setItem(48, createItem(Material.BOOK, LegacyText.YELLOW + "選択一括編集", LegacyText.GRAY + "選択数: " + LegacyText.WHITE + selectedCount));
        inv.setItem(50, createItem(Material.LAVA_BUCKET, LegacyText.RED + "選択一括削除", LegacyText.GRAY + "選択数: " + LegacyText.WHITE + selectedCount));

        inv.setItem(49, createItem(Material.BARRIER, LegacyText.RED + "戻る"));

        player.openInventory(inv);
    }

    public static void openGroupEdit(Player player, String groupName) {
        List<MythicSpawner> spawners = MythicBukkit.inst().getSpawnerManager().getSpawners().stream()
                .filter(s -> groupName.equalsIgnoreCase(s.getGroup()))
                .collect(Collectors.toList());

        MythicSpawner representative = spawners.isEmpty() ? null : spawners.get(0);

        Inventory inv = Bukkit.createInventory(null, 27, LegacyText.component(GROUP_EDIT_TITLE_PREFIX + groupName));

        String currentMob = representative != null ? representative.getTypeName() : "???";
        inv.setItem(0, createItem(Material.ZOMBIE_SPAWN_EGG, LegacyText.YELLOW + "モブ名の変更", 
                LegacyText.GRAY + "代表値: " + LegacyText.WHITE + currentMob,
                LegacyText.GRAY + "全スポナーの出現モブ(MobType)を変更します"));
        
        // Row 2: Basic options
        String maxMobs = representative != null ? String.valueOf(representative.getMaxMobs()) : "???";
        inv.setItem(9, createItem(Material.IRON_INGOT, LegacyText.YELLOW + "MaxMobs の変更", 
                LegacyText.GRAY + "代表値: " + LegacyText.WHITE + maxMobs,
                LegacyText.GRAY + "一括で変更します"));

        String mobLevel = representative != null ? String.valueOf(representative.getMobLevel()) : "???";
        inv.setItem(10, createItem(Material.EXPERIENCE_BOTTLE, LegacyText.YELLOW + "MobLevel の変更", 
                LegacyText.GRAY + "代表値: " + LegacyText.WHITE + mobLevel,
                LegacyText.GRAY + "一括で変更します"));

        String radius = representative != null ? String.valueOf(representative.getSpawnRadius()) : "???";
        inv.setItem(11, createItem(Material.COMPASS, LegacyText.YELLOW + "Radius の変更", 
                LegacyText.GRAY + "代表値: " + LegacyText.WHITE + radius,
                LegacyText.GRAY + "一括で変更します"));

        String activationRange = representative != null ? String.valueOf(representative.getActivationRange()) : "???";
        inv.setItem(12, createItem(Material.BEACON, LegacyText.YELLOW + "ActivationRange の変更", 
                LegacyText.GRAY + "代表値: " + LegacyText.WHITE + activationRange,
                LegacyText.GRAY + "一括で変更します"));

        String leashRange = representative != null ? String.valueOf(representative.getLeashRange()) : "???";
        inv.setItem(13, createItem(Material.LEAD, LegacyText.YELLOW + "LeashRange の変更", 
                LegacyText.GRAY + "代表値: " + LegacyText.WHITE + leashRange,
                LegacyText.GRAY + "一括で変更します"));

        String cooldown = representative != null ? String.valueOf(representative.getCooldownSeconds()) : "???";
        inv.setItem(14, createItem(Material.CLOCK, LegacyText.YELLOW + "Cooldown の変更", 
                LegacyText.GRAY + "代表値: " + LegacyText.WHITE + cooldown,
                LegacyText.GRAY + "一括で変更します"));

        String warmup = representative != null ? String.valueOf(representative.getWarmupSeconds()) : "???";
        inv.setItem(15, createItem(Material.CAMPFIRE, LegacyText.YELLOW + "Warmup の変更", 
                LegacyText.GRAY + "代表値: " + LegacyText.WHITE + warmup,
                LegacyText.GRAY + "一括で変更します"));

        String amountValue = "???";
        if (representative != null) {
            try {
                java.lang.reflect.Method m = representative.getClass().getMethod("getMobsPerSpawn");
                amountValue = String.valueOf(m.invoke(representative));
            } catch (Exception ignored) {}
        }
        inv.setItem(16, createItem(Material.SLIME_BALL, LegacyText.YELLOW + "Amount の変更", 
                LegacyText.GRAY + "代表値: " + LegacyText.WHITE + amountValue,
                LegacyText.GRAY + "一括で変更します"));

        inv.setItem(22, createItem(Material.IRON_DOOR, LegacyText.GRAY + "戻る"));
        
        player.openInventory(inv);
    }

    public static void openIndividualEdit(Player player, String spawnerName) {
        MythicSpawner s = MythicBukkit.inst().getSpawnerManager().getSpawnerByName(spawnerName);
        if (s == null) {
            player.sendMessage(LegacyText.RED + "スポナーが見つかりませんでした。");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, LegacyText.component(INDIVIDUAL_EDIT_TITLE_PREFIX + spawnerName));

        inv.setItem(0, createItem(Material.ZOMBIE_SPAWN_EGG, LegacyText.YELLOW + "モブ名の変更", 
                LegacyText.GRAY + "現在の値: " + LegacyText.WHITE + s.getTypeName(),
                LegacyText.GRAY + "このスポナーの出現モブ(MobType)を変更します",
                "", LegacyText.AQUA + "右クリックで元に戻す"));

        // Row 2: Basic options
        inv.setItem(9, createItem(Material.IRON_INGOT, LegacyText.YELLOW + "MaxMobs の変更", 
                LegacyText.GRAY + "現在の値: " + LegacyText.WHITE + s.getMaxMobs(),
                LegacyText.GRAY + "設定値を変更します",
                "", LegacyText.AQUA + "右クリックで元に戻す"));
        inv.setItem(10, createItem(Material.EXPERIENCE_BOTTLE, LegacyText.YELLOW + "MobLevel の変更", 
                LegacyText.GRAY + "現在の値: " + LegacyText.WHITE + s.getMobLevel(),
                LegacyText.GRAY + "設定値を変更します",
                "", LegacyText.AQUA + "右クリックで元に戻す"));
        inv.setItem(11, createItem(Material.COMPASS, LegacyText.YELLOW + "Radius の変更", 
                LegacyText.GRAY + "現在の値: " + LegacyText.WHITE + s.getSpawnRadius(),
                LegacyText.GRAY + "設定値を変更します",
                "", LegacyText.AQUA + "右クリックで元に戻す"));
        inv.setItem(12, createItem(Material.BEACON, LegacyText.YELLOW + "ActivationRange の変更", 
                LegacyText.GRAY + "現在の値: " + LegacyText.WHITE + s.getActivationRange(),
                LegacyText.GRAY + "設定値を変更します",
                "", LegacyText.AQUA + "右クリックで元に戻す"));
        inv.setItem(13, createItem(Material.LEAD, LegacyText.YELLOW + "LeashRange の変更", 
                LegacyText.GRAY + "現在の値: " + LegacyText.WHITE + s.getLeashRange(),
                LegacyText.GRAY + "設定値を変更します",
                "", LegacyText.AQUA + "右クリックで元に戻す"));
        inv.setItem(14, createItem(Material.CLOCK, LegacyText.YELLOW + "Cooldown の変更", 
                LegacyText.GRAY + "現在の値: " + LegacyText.WHITE + s.getCooldownSeconds(),
                LegacyText.GRAY + "設定値を変更します",
                "", LegacyText.AQUA + "右クリックで元に戻す"));
        inv.setItem(15, createItem(Material.CAMPFIRE, LegacyText.YELLOW + "Warmup の変更", 
                LegacyText.GRAY + "現在の値: " + LegacyText.WHITE + s.getWarmupSeconds(),
                LegacyText.GRAY + "設定値を変更します",
                "", LegacyText.AQUA + "右クリックで元に戻す"));
        
        String amount = "???";
        try {
            java.lang.reflect.Method m = s.getClass().getMethod("getMobsPerSpawn");
            amount = String.valueOf(m.invoke(s));
        } catch (Exception ignored) {}

        inv.setItem(16, createItem(Material.SLIME_BALL, LegacyText.YELLOW + "Amount の変更", 
                LegacyText.GRAY + "現在の値: " + LegacyText.WHITE + amount,
                LegacyText.GRAY + "設定値を変更します",
                "", LegacyText.AQUA + "右クリックで元に戻す"));

        inv.setItem(22, createItem(Material.IRON_DOOR, LegacyText.GRAY + "戻る"));

        player.openInventory(inv);
    }

    public static void openDetail(Player player, String spawnerName) {
        MythicSpawner s = MythicBukkit.inst().getSpawnerManager().getSpawnerByName(spawnerName);
        if (s == null) {
            player.sendMessage(LegacyText.RED + "スポナーが見つかりませんでした。");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, LegacyText.component(DETAIL_TITLE_PREFIX + spawnerName));

        inv.setItem(4, createSpawnerItem(s));
        inv.setItem(10, createItem(Material.WRITABLE_BOOK, LegacyText.YELLOW + "編集", LegacyText.GRAY + "このスポナーの設定を変更します"));
        inv.setItem(12, createItem(Material.ENDER_PEARL, LegacyText.AQUA + "テレポート", LegacyText.GRAY + "スポナーの座標にテレポートします"));
        inv.setItem(14, createItem(Material.BEACON, LegacyText.YELLOW + "位置を可視化", LegacyText.GRAY + "パーティクルで場所を表示します"));
        inv.setItem(16, createItem(Material.BARRIER, LegacyText.RED + "削除", LegacyText.GRAY + "このスポナーを削除します"));
        inv.setItem(22, createItem(Material.IRON_DOOR, LegacyText.GRAY + "戻る"));

        player.openInventory(inv);
    }

    private static ItemStack createSpawnerItem(MythicSpawner s) {
        ItemStack item = new ItemStack(Material.SPAWNER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(LegacyText.component(LegacyText.GOLD + s.getName()));
        List<String> lore = new ArrayList<>();
        lore.add(LegacyText.YELLOW + "Mob: " + LegacyText.WHITE + s.getTypeName());
        lore.add(LegacyText.YELLOW + "Group: " + LegacyText.WHITE + (s.getGroup() == null ? "なし" : s.getGroup()));
        lore.add(LegacyText.YELLOW + "World: " + LegacyText.WHITE + s.getLocation().getWorld().getName());
        lore.add(LegacyText.YELLOW + "Location: " + LegacyText.WHITE + String.format("%.0f, %.0f, %.0f", s.getLocation().getX(), s.getLocation().getY(), s.getLocation().getZ()));
        lore.add("");
        lore.add(LegacyText.GRAY + "--- 設定 ---");
        lore.add(LegacyText.GRAY + "MaxMobs: " + s.getMaxMobs());
        lore.add(LegacyText.GRAY + "MobLevel: " + s.getMobLevel());
        lore.add(LegacyText.GRAY + "Cooldown: " + s.getCooldownSeconds());
        lore.add(LegacyText.GRAY + "Warmup: " + s.getWarmupSeconds());
        lore.add(LegacyText.GRAY + "Radius: " + s.getSpawnRadius());
        lore.add(LegacyText.GRAY + "ActivationRange: " + s.getActivationRange());
        lore.add(LegacyText.GRAY + "LeashRange: " + s.getLeashRange());

        String amount = "???";
        try {
            java.lang.reflect.Method m = s.getClass().getMethod("getMobsPerSpawn");
            amount = String.valueOf(m.invoke(s));
        } catch (Exception ignored) {}
        lore.add(LegacyText.GRAY + "Amount: " + amount);

        lore.add(LegacyText.GRAY + "CheckForPlayers: " + s.isCheckForPlayers());
        lore.add("");
        lore.add(LegacyText.WHITE + "左クリック: 詳細表示");
        lore.add(LegacyText.WHITE + "シフト左クリック: 編集");
        lore.add(LegacyText.WHITE + "右クリック: 位置を可視化");
        meta.lore(LegacyText.components(lore));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createSpawnerItemWithSelectState(MythicSpawner s, boolean selected, boolean selectionMode) {
        ItemStack base = createSpawnerItem(s);
        ItemMeta meta = base.getItemMeta();
        String name = LegacyText.GOLD + s.getInternalName();
        if (selected) {
            name = LegacyText.GREEN + "[選択] " + LegacyText.GOLD + s.getInternalName();
        }
        meta.displayName(LegacyText.component(name));
        List<String> lore = meta.lore() == null ? new ArrayList<>() : meta.lore().stream().map(LegacyText::serialize).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        lore.add(0, selected ? LegacyText.GREEN + "このスポナーは選択されています" : LegacyText.GRAY + "未選択");
        lore.add("");
        if (selectionMode) {
            lore.add(LegacyText.WHITE + "左クリック: 選択/解除");
            lore.add(LegacyText.WHITE + "右クリック: 選択/解除");
        } else {
            lore.add(LegacyText.WHITE + "左クリック: 詳細表示");
            lore.add(LegacyText.WHITE + "シフト左クリック: 編集");
            lore.add(LegacyText.WHITE + "右クリック: 位置を可視化");
        }
        meta.lore(LegacyText.components(lore));
        // 視認性向上のため、選択中のスポナーにはエンチャント光を付与
        if (selected) {
            try {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
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
        meta.displayName(LegacyText.component(name));
        if (lore.length > 0) {
            meta.lore(LegacyText.components(Arrays.asList(lore)));
        }
        item.setItemMeta(meta);
        return item;
    }

    public static void openSelectedEdit(Player player, java.util.Set<String> selectedNames) {
        String title = SELECT_EDIT_TITLE;
        Inventory inv = Bukkit.createInventory(null, 27, LegacyText.component(title));

        MythicSpawner representative = null;
        for (String name : selectedNames) {
            representative = MythicBukkit.inst().getSpawnerManager().getSpawnerByName(name);
            if (representative != null) break;
        }

        String currentMob = representative != null ? representative.getTypeName() : "???";
        inv.setItem(0, createItem(Material.ZOMBIE_SPAWN_EGG, LegacyText.YELLOW + "モブ名の変更",
                LegacyText.GRAY + "代表値: " + LegacyText.WHITE + currentMob,
                LegacyText.GRAY + "選択中のスポナーを一括変更します"));

        String maxMobs = representative != null ? String.valueOf(representative.getMaxMobs()) : "???";
        inv.setItem(9, createItem(Material.IRON_INGOT, LegacyText.YELLOW + "MaxMobs の変更",
                LegacyText.GRAY + "代表値: " + LegacyText.WHITE + maxMobs,
                LegacyText.GRAY + "一括で変更します"));

        String mobLevel = representative != null ? String.valueOf(representative.getMobLevel()) : "???";
        inv.setItem(10, createItem(Material.EXPERIENCE_BOTTLE, LegacyText.YELLOW + "MobLevel の変更",
                LegacyText.GRAY + "代表値: " + LegacyText.WHITE + mobLevel,
                LegacyText.GRAY + "一括で変更します"));

        String radius = representative != null ? String.valueOf(representative.getSpawnRadius()) : "???";
        inv.setItem(11, createItem(Material.COMPASS, LegacyText.YELLOW + "Radius の変更",
                LegacyText.GRAY + "代表値: " + LegacyText.WHITE + radius,
                LegacyText.GRAY + "一括で変更します"));

        String activationRange = representative != null ? String.valueOf(representative.getActivationRange()) : "???";
        inv.setItem(12, createItem(Material.BEACON, LegacyText.YELLOW + "ActivationRange の変更",
                LegacyText.GRAY + "代表値: " + LegacyText.WHITE + activationRange,
                LegacyText.GRAY + "一括で変更します"));

        String leashRange = representative != null ? String.valueOf(representative.getLeashRange()) : "???";
        inv.setItem(13, createItem(Material.LEAD, LegacyText.YELLOW + "LeashRange の変更",
                LegacyText.GRAY + "代表値: " + LegacyText.WHITE + leashRange,
                LegacyText.GRAY + "一括で変更します"));

        String cooldown = representative != null ? String.valueOf(representative.getCooldownSeconds()) : "???";
        inv.setItem(14, createItem(Material.CLOCK, LegacyText.YELLOW + "Cooldown の変更",
                LegacyText.GRAY + "代表値: " + LegacyText.WHITE + cooldown,
                LegacyText.GRAY + "一括で変更します"));

        String warmup = representative != null ? String.valueOf(representative.getWarmupSeconds()) : "???";
        inv.setItem(15, createItem(Material.CAMPFIRE, LegacyText.YELLOW + "Warmup の変更",
                LegacyText.GRAY + "代表値: " + LegacyText.WHITE + warmup,
                LegacyText.GRAY + "一括で変更します"));

        String amountValue = "???";
        if (representative != null) {
            try {
                java.lang.reflect.Method m = representative.getClass().getMethod("getMobsPerSpawn");
                amountValue = String.valueOf(m.invoke(representative));
            } catch (Exception ignored) {}
        }
        inv.setItem(16, createItem(Material.SLIME_BALL, LegacyText.YELLOW + "Amount の変更",
                LegacyText.GRAY + "代表値: " + LegacyText.WHITE + amountValue,
                LegacyText.GRAY + "一括で変更します"));

        inv.setItem(22, createItem(Material.IRON_DOOR, LegacyText.GRAY + "戻る"));

        player.openInventory(inv);
    }

    public static void openConfirmBulkDeleteSelected(Player player, int count) {
        Inventory inv = Bukkit.createInventory(null, 27, LegacyText.component(CONFIRM_DELETE_SELECTED_TITLE));
        inv.setItem(11, createItem(Material.LIME_WOOL, LegacyText.GREEN + "実行",
                LegacyText.GRAY + "選択された " + LegacyText.WHITE + count + LegacyText.GRAY + " 個を削除します"));
        inv.setItem(15, createItem(Material.RED_WOOL, LegacyText.RED + "キャンセル"));
        inv.setItem(22, createItem(Material.IRON_DOOR, LegacyText.GRAY + "戻る"));
        player.openInventory(inv);
    }
}
