package net.azisaba.lifemoremythicmobs.listener;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.spawning.spawners.MythicSpawner;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.gui.SpawnerManagerGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpawnerManagerListener implements Listener {

    private final LifeMoreMythicMobs plugin;
    private final Map<UUID, String> currentFilterType = new HashMap<>();
    private final Map<UUID, String> currentFilterValue = new HashMap<>();
    private final Map<UUID, Integer> currentPage = new HashMap<>();
    private final Map<UUID, String> pendingGroupEditSetting = new HashMap<>();
    private final Map<UUID, String> pendingIndividualSpawner = new HashMap<>();
    private final Map<UUID, String> pendingIndividualEditSetting = new HashMap<>();
    private final Map<UUID, Map<String, String>> originalIndividualSettings = new HashMap<>();
    // Multi-select state
    private final Map<UUID, java.util.Set<String>> selectedSpawners = new HashMap<>();
    private final Map<UUID, Boolean> selectionMode = new HashMap<>();
    private final Map<UUID, String> pendingSelectedEditSetting = new HashMap<>();

    public SpawnerManagerListener(LifeMoreMythicMobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith("スポナー管理") && 
            !title.startsWith(SpawnerManagerGUI.SPAWNER_LIST_TITLE_PREFIX) && 
            !title.startsWith(SpawnerManagerGUI.DETAIL_TITLE_PREFIX) &&
            !title.startsWith(SpawnerManagerGUI.GROUP_EDIT_TITLE_PREFIX) &&
            !title.startsWith(SpawnerManagerGUI.INDIVIDUAL_EDIT_TITLE_PREFIX) &&
            !title.equals(SpawnerManagerGUI.SELECT_EDIT_TITLE) &&
            !title.equals(SpawnerManagerGUI.CONFIRM_DELETE_SELECTED_TITLE)) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getRawSlot();

        if (title.equals(SpawnerManagerGUI.MAIN_TITLE)) {
            if (slot == 2) {
                clearSelection(player);
                currentPage.put(player.getUniqueId(), 0);
                SpawnerManagerGUI.openWorldList(player, 0);
            }
            else if (slot == 6) {
                clearSelection(player);
                currentPage.put(player.getUniqueId(), 0);
                SpawnerManagerGUI.openGroupList(player, 0);
            }
        } 
        else if (title.equals(SpawnerManagerGUI.WORLD_LIST_TITLE)) {
            if (slot < 45) {
                String worldName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                currentFilterType.put(player.getUniqueId(), "world");
                currentFilterValue.put(player.getUniqueId(), worldName);
                currentPage.put(player.getUniqueId(), 0);
                clearSelection(player);
                SpawnerManagerGUI.openSpawnerList(player, "world", worldName, 0, getSelected(player), isSelectionMode(player));
            } else if (slot == 45) {
                int page = currentPage.getOrDefault(player.getUniqueId(), 0) - 1;
                if (page >= 0) {
                    currentPage.put(player.getUniqueId(), page);
                    SpawnerManagerGUI.openWorldList(player, page);
                }
            } else if (slot == 53) {
                int page = currentPage.getOrDefault(player.getUniqueId(), 0) + 1;
                currentPage.put(player.getUniqueId(), page);
                SpawnerManagerGUI.openWorldList(player, page);
            } else if (slot == 49) {
                clearSelection(player);
                SpawnerManagerGUI.openMain(player);
            }
        } 
        else if (title.equals(SpawnerManagerGUI.GROUP_LIST_TITLE)) {
            if (slot < 45) {
                String groupName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                currentFilterType.put(player.getUniqueId(), "group");
                currentFilterValue.put(player.getUniqueId(), groupName);
                currentPage.put(player.getUniqueId(), 0);
                clearSelection(player);
                SpawnerManagerGUI.openSpawnerList(player, "group", groupName, 0, getSelected(player), isSelectionMode(player));
            } else if (slot == 45) {
                int page = currentPage.getOrDefault(player.getUniqueId(), 0) - 1;
                if (page >= 0) {
                    currentPage.put(player.getUniqueId(), page);
                    SpawnerManagerGUI.openGroupList(player, page);
                }
            } else if (slot == 53) {
                int page = currentPage.getOrDefault(player.getUniqueId(), 0) + 1;
                currentPage.put(player.getUniqueId(), page);
                SpawnerManagerGUI.openGroupList(player, page);
            } else if (slot == 49) {
                clearSelection(player);
                SpawnerManagerGUI.openMain(player);
            }
        } 
        else if (title.startsWith(SpawnerManagerGUI.SPAWNER_LIST_TITLE_PREFIX)) {
            if (slot < 45) {
                String spawnerName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                if (spawnerName.startsWith("[選択] ")) {
                    spawnerName = spawnerName.substring("[選択] ".length());
                }
                // 選択モードならトグル
                if (isSelectionMode(player)) {
                    java.util.Set<String> set = getSelected(player);
                    if (set.contains(spawnerName)) {
                        set.remove(spawnerName);
                        player.sendMessage(ChatColor.YELLOW + "選択解除: " + spawnerName + ChatColor.GRAY + " (選択数: " + set.size() + ")");
                    } else {
                        set.add(spawnerName);
                        player.sendMessage(ChatColor.GREEN + "選択: " + spawnerName + ChatColor.GRAY + " (選択数: " + set.size() + ")");
                    }
                    SpawnerManagerGUI.openSpawnerList(player, currentFilterType.get(player.getUniqueId()), currentFilterValue.get(player.getUniqueId()), currentPage.getOrDefault(player.getUniqueId(), 0), set, true);
                } else {
                    if (event.getClick() == ClickType.RIGHT) {
                        visualizeSpawner(player, spawnerName);
                    } else if (event.getClick() == ClickType.SHIFT_LEFT) {
                        saveOriginalSettings(player, spawnerName);
                        SpawnerManagerGUI.openIndividualEdit(player, spawnerName);
                    } else {
                        SpawnerManagerGUI.openDetail(player, spawnerName);
                    }
                }
            } else if (slot == 47 && currentFilterType.getOrDefault(player.getUniqueId(), "").equals("group")) {
                SpawnerManagerGUI.openGroupEdit(player, currentFilterValue.get(player.getUniqueId()));
            } else if (slot == 46) { // 選択モード切替
                boolean now = !isSelectionMode(player);
                selectionMode.put(player.getUniqueId(), now);
                SpawnerManagerGUI.openSpawnerList(player, currentFilterType.get(player.getUniqueId()), currentFilterValue.get(player.getUniqueId()), currentPage.getOrDefault(player.getUniqueId(), 0), getSelected(player), now);
            } else if (slot == 48) { // 選択一括編集
                java.util.Set<String> set = getSelected(player);
                if (set.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "選択されていません。");
                    return;
                }
                SpawnerManagerGUI.openSelectedEdit(player, set);
            } else if (slot == 50) { // 選択一括削除
                java.util.Set<String> set = getSelected(player);
                if (set.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "選択されていません。");
                    return;
                }
                SpawnerManagerGUI.openConfirmBulkDeleteSelected(player, set.size());
            } else if (slot == 45) {
                int page = currentPage.getOrDefault(player.getUniqueId(), 0) - 1;
                currentPage.put(player.getUniqueId(), page);
                SpawnerManagerGUI.openSpawnerList(player, currentFilterType.get(player.getUniqueId()), currentFilterValue.get(player.getUniqueId()), page, getSelected(player), isSelectionMode(player));
            } else if (slot == 53) {
                int page = currentPage.getOrDefault(player.getUniqueId(), 0) + 1;
                currentPage.put(player.getUniqueId(), page);
                SpawnerManagerGUI.openSpawnerList(player, currentFilterType.get(player.getUniqueId()), currentFilterValue.get(player.getUniqueId()), page, getSelected(player), isSelectionMode(player));
            } else if (slot == 49) {
                clearSelection(player);
                SpawnerManagerGUI.openMain(player);
            }
        } 
        else if (title.startsWith(SpawnerManagerGUI.DETAIL_TITLE_PREFIX)) {
            String spawnerName = title.substring(SpawnerManagerGUI.DETAIL_TITLE_PREFIX.length());
            if (slot == 10) { // Edit
                saveOriginalSettings(player, spawnerName);
                SpawnerManagerGUI.openIndividualEdit(player, spawnerName);
            } else if (slot == 12) { // Teleport
                MythicSpawner s = MythicMobs.inst().getSpawnerManager().getSpawnerByName(spawnerName);
                if (s != null) {
                    player.teleport(BukkitAdapter.adapt(s.getLocation()));
                    player.sendMessage(ChatColor.GREEN + "テレポートしました: " + spawnerName);
                }
            } else if (slot == 14) { // Visualize
                visualizeSpawner(player, spawnerName);
            } else if (slot == 16) { // Delete
                player.performCommand("mm spawners remove " + spawnerName);
                player.sendMessage(ChatColor.RED + "スポナーを削除しました: " + spawnerName);
                SpawnerManagerGUI.openSpawnerList(player, currentFilterType.get(player.getUniqueId()), currentFilterValue.get(player.getUniqueId()), currentPage.getOrDefault(player.getUniqueId(), 0), getSelected(player), isSelectionMode(player));
            } else if (slot == 22) { // Back
                SpawnerManagerGUI.openSpawnerList(player, currentFilterType.get(player.getUniqueId()), currentFilterValue.get(player.getUniqueId()), currentPage.getOrDefault(player.getUniqueId(), 0), getSelected(player), isSelectionMode(player));
            }
        }
        else if (title.startsWith(SpawnerManagerGUI.INDIVIDUAL_EDIT_TITLE_PREFIX)) {
            String spawnerName = title.substring(SpawnerManagerGUI.INDIVIDUAL_EDIT_TITLE_PREFIX.length());
            if (slot == 22) { // Back
                SpawnerManagerGUI.openDetail(player, spawnerName);
                return;
            }

            String setting = null;
            switch (slot) {
                case 0: setting = "mobtype"; break;
                case 9: setting = "maxmobs"; break;
                case 10: setting = "moblevel"; break;
                case 11: setting = "radius"; break;
                case 12: setting = "activationrange"; break;
                case 13: setting = "leashrange"; break;
                case 14: setting = "cooldown"; break;
                case 15: setting = "warmup"; break;
                case 16: setting = "amount"; break;
            }

            if (setting != null) {
                if (event.getClick() == ClickType.RIGHT) {
                    Map<String, String> original = originalIndividualSettings.get(player.getUniqueId());
                    if (original != null && original.containsKey(setting)) {
                        String originalValue = original.get(setting);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm s set " + spawnerName + " " + setting + " " + originalValue);
                        player.sendMessage(ChatColor.GREEN + spawnerName + " の " + setting + " を編集前の値 (" + originalValue + ") に戻しました。");
                        SpawnerManagerGUI.openIndividualEdit(player, spawnerName);
                    } else {
                        player.sendMessage(ChatColor.RED + "編集前の値が見つかりません。");
                    }
                    return;
                }

                pendingIndividualSpawner.put(player.getUniqueId(), spawnerName);
                pendingIndividualEditSetting.put(player.getUniqueId(), setting);
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "変更する値をチャットで入力してください (" + setting + ")");
                player.sendMessage(ChatColor.GRAY + "キャンセルするには 'cancel' と入力してください。");
            }
        }
        else if (title.startsWith(SpawnerManagerGUI.GROUP_EDIT_TITLE_PREFIX)) {
            if (slot == 22) { // Back
                SpawnerManagerGUI.openSpawnerList(player, currentFilterType.get(player.getUniqueId()), currentFilterValue.get(player.getUniqueId()), currentPage.getOrDefault(player.getUniqueId(), 0), getSelected(player), isSelectionMode(player));
                return;
            }

            String setting = null;
            switch (slot) {
                case 0: setting = "mobtype"; break;
                case 9: setting = "maxmobs"; break;
                case 10: setting = "moblevel"; break;
                case 11: setting = "radius"; break;
                case 12: setting = "activationrange"; break;
                case 13: setting = "leashrange"; break;
                case 14: setting = "cooldown"; break;
                case 15: setting = "warmup"; break;
                case 16: setting = "amount"; break;
            }

            if (setting != null) {
                pendingGroupEditSetting.put(player.getUniqueId(), setting);
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "一括変更する値をチャットで入力してください (" + setting + ")");
                player.sendMessage(ChatColor.GRAY + "キャンセルするには 'cancel' と入力してください。");
            }
        }
        else if (title.equals(SpawnerManagerGUI.SELECT_EDIT_TITLE)) {
            if (slot == 22) { // Back
                SpawnerManagerGUI.openSpawnerList(player, currentFilterType.get(player.getUniqueId()), currentFilterValue.get(player.getUniqueId()), currentPage.getOrDefault(player.getUniqueId(), 0), getSelected(player), isSelectionMode(player));
                return;
            }

            String setting = null;
            switch (slot) {
                case 0: setting = "mobtype"; break;
                case 9: setting = "maxmobs"; break;
                case 10: setting = "moblevel"; break;
                case 11: setting = "radius"; break;
                case 12: setting = "activationrange"; break;
                case 13: setting = "leashrange"; break;
                case 14: setting = "cooldown"; break;
                case 15: setting = "warmup"; break;
                case 16: setting = "amount"; break;
            }

            if (setting != null) {
                pendingSelectedEditSetting.put(player.getUniqueId(), setting);
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "一括変更する値をチャットで入力してください (" + setting + ")");
                player.sendMessage(ChatColor.GRAY + "キャンセルするには 'cancel' と入力してください。");
            }
        }
        else if (title.equals(SpawnerManagerGUI.CONFIRM_DELETE_SELECTED_TITLE)) {
            if (slot == 11) {
                java.util.Set<String> set = getSelected(player);
                if (set.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "選択されていません。");
                    SpawnerManagerGUI.openSpawnerList(player, currentFilterType.get(player.getUniqueId()), currentFilterValue.get(player.getUniqueId()), currentPage.getOrDefault(player.getUniqueId(), 0), set, isSelectionMode(player));
                    return;
                }
                player.closeInventory();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (String name : new java.util.HashSet<>(set)) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm s remove " + name);
                        }
                        player.sendMessage(ChatColor.RED + "選択した " + set.size() + " 個のスポナーを削除しました。");
                        clearSelection(player);
                        SpawnerManagerGUI.openSpawnerList(player, currentFilterType.get(player.getUniqueId()), currentFilterValue.get(player.getUniqueId()), currentPage.getOrDefault(player.getUniqueId(), 0), getSelected(player), isSelectionMode(player));
                    }
                }.runTask(plugin);
            } else if (slot == 15 || slot == 22) {
                SpawnerManagerGUI.openSpawnerList(player, currentFilterType.get(player.getUniqueId()), currentFilterValue.get(player.getUniqueId()), currentPage.getOrDefault(player.getUniqueId(), 0), getSelected(player), isSelectionMode(player));
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        // レガシーなチャットイベント（1.15系〜）
        Player player = event.getPlayer();
        String message = event.getMessage();
        boolean handled = handleChatInput(player, message);
        if (handled) {
            event.setCancelled(true);
        }
    }

    // 注意: Bukkit のリスナーはベースの org.bukkit.event.Event では登録できないため、
    // ここでの Paper 1.19+ の AsyncChatEvent 反映用リフレクションハンドラは削除しました。
    // 本プラグインは 1.15 系ターゲットのため、従来の AsyncPlayerChatEvent ハンドラ（onChat）で十分に動作します。

    // 共通のチャット入力処理。処理した場合 true を返す（元のチャット送信はキャンセルされるべき）。
    private boolean handleChatInput(Player player, String rawMessage) {
        // どの待機状態でもなければ処理しない
        boolean waitingIndividual = pendingIndividualEditSetting.containsKey(player.getUniqueId());
        boolean waitingSelected = pendingSelectedEditSetting.containsKey(player.getUniqueId());
        boolean waitingGroup = pendingGroupEditSetting.containsKey(player.getUniqueId());
        if (!waitingIndividual && !waitingSelected && !waitingGroup) return false;

        String value = rawMessage;

        if (waitingIndividual) {
            String spawnerName = pendingIndividualSpawner.remove(player.getUniqueId());
            String setting = pendingIndividualEditSetting.remove(player.getUniqueId());

            if (value.equalsIgnoreCase("cancel")) {
                player.sendMessage(ChatColor.RED + "編集をキャンセルしました。");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        SpawnerManagerGUI.openIndividualEdit(player, spawnerName);
                    }
                }.runTask(plugin);
                return true;
            }

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm s set " + spawnerName + " " + setting + " " + value);
            player.sendMessage(ChatColor.GREEN + spawnerName + " の " + setting + " を " + value + " に変更しました。");

            new BukkitRunnable() {
                @Override
                public void run() {
                    SpawnerManagerGUI.openIndividualEdit(player, spawnerName);
                }
            }.runTask(plugin);
            return true;
        }

        if (waitingSelected) {
            String setting = pendingSelectedEditSetting.remove(player.getUniqueId());

            if (value.equalsIgnoreCase("cancel")) {
                player.sendMessage(ChatColor.RED + "編集をキャンセルしました。");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        SpawnerManagerGUI.openSelectedEdit(player, getSelected(player));
                    }
                }.runTask(plugin);
                return true;
            }

            java.util.Set<String> set = getSelected(player);
            if (set.isEmpty()) {
                player.sendMessage(ChatColor.RED + "選択が空です。");
                return true;
            }

            player.sendMessage(ChatColor.GREEN + "選択中のスポナー " + set.size() + " 個の " + setting + " を " + value + " に変更しています...");
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (String name : set) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm s set " + name + " " + setting + " " + value);
                    }
                    player.sendMessage(ChatColor.GREEN + "変更が完了しました。");
                    SpawnerManagerGUI.openSelectedEdit(player, set);
                }
            }.runTask(plugin);
            return true;
        }

        if (waitingGroup) {
            String setting = pendingGroupEditSetting.remove(player.getUniqueId());

            if (value.equalsIgnoreCase("cancel")) {
                player.sendMessage(ChatColor.RED + "編集をキャンセルしました。");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        SpawnerManagerGUI.openGroupEdit(player, currentFilterValue.get(player.getUniqueId()));
                    }
                }.runTask(plugin);
                return true;
            }

            String group = currentFilterValue.get(player.getUniqueId());
            java.util.List<MythicSpawner> spawners = MythicMobs.inst().getSpawnerManager().getSpawners().stream()
                    .filter(s -> group.equalsIgnoreCase(s.getGroup()))
                    .collect(java.util.stream.Collectors.toList());

            if (spawners.isEmpty()) {
                player.sendMessage(ChatColor.RED + "対象のスポナーが見つかりませんでした。");
                return true;
            }

            player.sendMessage(ChatColor.GREEN + group + " グループのスポナー " + spawners.size() + " 個の " + setting + " を " + value + " に変更しています...");

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (MythicSpawner s : spawners) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm s set " + s.getInternalName() + " " + setting + " " + value);
                    }
                    player.sendMessage(ChatColor.GREEN + "変更が完了しました。");
                    SpawnerManagerGUI.openGroupEdit(player, group);
                }
            }.runTask(plugin);
            return true;
        }

        return false;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity.hasMetadata("LMMM_VISUALIZE_SPAWNER")) {
            event.setCancelled(true);
            String spawnerName = entity.getMetadata("LMMM_VISUALIZE_SPAWNER").get(0).asString();
            SpawnerManagerGUI.openDetail(event.getPlayer(), spawnerName);
        }
    }

    private void visualizeSpawner(Player player, String spawnerName) {
        MythicSpawner s = MythicMobs.inst().getSpawnerManager().getSpawnerByName(spawnerName);
        if (s == null) return;

        AbstractLocation aloc = s.getLocation();
        Location loc = BukkitAdapter.adapt(aloc).add(0.5, 0.5, 0.5);
        
        player.sendMessage(ChatColor.GREEN + "スポナー '" + spawnerName + "' の位置を可視化しています (10秒間)");

        org.bukkit.entity.FallingBlock fb = loc.getWorld().spawnFallingBlock(loc, org.bukkit.Material.SPAWNER, (byte) 0);
        fb.setGravity(false);
        fb.setGlowing(true);
        fb.setDropItem(false);
        fb.setInvulnerable(true);
        fb.setMetadata("LMMM_VISUALIZE_SPAWNER", new FixedMetadataValue(plugin, spawnerName));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (fb.isValid()) {
                    fb.remove();
                }
            }
        }.runTaskLater(plugin, 200L);
    }

    private void saveOriginalSettings(Player player, String spawnerName) {
        MythicSpawner s = MythicMobs.inst().getSpawnerManager().getSpawnerByName(spawnerName);
        if (s == null) return;

        Map<String, String> settings = new HashMap<>();
        settings.put("mobtype", s.getTypeName());
        settings.put("maxmobs", String.valueOf(s.getMaxMobs()));
        settings.put("moblevel", String.valueOf(s.getMobLevel()));
        settings.put("radius", String.valueOf(s.getSpawnRadius()));
        settings.put("activationrange", String.valueOf(s.getActivationRange()));
        settings.put("leashrange", String.valueOf(s.getLeashRange()));
        settings.put("cooldown", String.valueOf(s.getCooldownSeconds()));
        settings.put("warmup", String.valueOf(s.getWarmupSeconds()));
        
        String amount = "???";
        try {
            java.lang.reflect.Method m = s.getClass().getMethod("getMobsPerSpawn");
            amount = String.valueOf(m.invoke(s));
        } catch (Exception ignored) {}
        settings.put("amount", amount);

        originalIndividualSettings.put(player.getUniqueId(), settings);
    }

    private java.util.Set<String> getSelected(Player player) {
        return selectedSpawners.computeIfAbsent(player.getUniqueId(), k -> new java.util.HashSet<>());
    }

    private boolean isSelectionMode(Player player) {
        return selectionMode.getOrDefault(player.getUniqueId(), false);
    }

    private void clearSelection(Player player) {
        java.util.Set<String> set = selectedSpawners.get(player.getUniqueId());
        if (set != null) set.clear();
        selectionMode.put(player.getUniqueId(), false);
    }
}
