package net.azisaba.lifemoremythicmobs.listener;

import io.lumine.mythic.bukkit.MythicBukkit;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.gui.SpawnerToolGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpawnerToolListener implements Listener {
    private final LifeMoreMythicMobs plugin;
    private final Map<UUID, String> waitingForMobName = new HashMap<>();
    private final Map<UUID, String> waitingForSpawnerName = new HashMap<>();
    private final Map<UUID, String> waitingForGroupName = new HashMap<>();
    private final Map<UUID, String> waitingForOption = new HashMap<>();
    private final Map<UUID, Map<String, String>> sessionData = new HashMap<>();

    private static final NamespacedKey KEY_MOB = new NamespacedKey("lifemoremythicmobs", "spawner_tool_mob");
    private static final NamespacedKey KEY_NAME = new NamespacedKey("lifemoremythicmobs", "spawner_tool_name");
    private static final NamespacedKey KEY_GROUP = new NamespacedKey("lifemoremythicmobs", "spawner_tool_group");

    public SpawnerToolListener(LifeMoreMythicMobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(SpawnerToolGUI.TITLE)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        boolean isRightClick = event.getClick().isRightClick();

        if (slot == 1) {
            if (isRightClick) {
                removeData(player, "mob");
                return;
            }
            waitingForMobName.put(player.getUniqueId(), "mob");
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "チャットにMythicMobの名前を入力してください。");
        } else if (slot == 3) {
            if (isRightClick) {
                removeData(player, "spawner");
                return;
            }
            waitingForSpawnerName.put(player.getUniqueId(), "spawner");
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "チャットにスポナーのベース名を入力してください。");
        } else if (slot == 5) {
            if (isRightClick) {
                removeData(player, "group");
                return;
            }
            waitingForGroupName.put(player.getUniqueId(), "group");
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "チャットにスポナーのグループ名を入力してください。");
        } else if (slot == 8) {
            complete(player);
        } else if (slot >= 9 && slot < 18) {
            String[] options = {"MaxMobs", "MobLevel", "Radius", "ActivationRange", "LeashRange", "Cooldown", "Warmup", "MaxDistance", "CheckForPlayers"};
            String opt = options[slot - 9];
            if (isRightClick) {
                removeData(player, "opt." + opt);
                return;
            }
            waitingForOption.put(player.getUniqueId(), opt);
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "チャットにオプション '" + opt + "' の値を入力してください。");
        } else if (slot >= 18 && slot < 27) {
            String[] options = {"UseWorldScaling", "PlayerRange", "Amount"};
            int index = slot - 18;
            if (index < options.length) {
                String opt = options[index];
                if (isRightClick) {
                    removeData(player, "opt." + opt);
                    return;
                }
                waitingForOption.put(player.getUniqueId(), opt);
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "チャットにオプション '" + opt + "' の値を入力してください。");
            }
        } else if (slot == 26) {
            sessionData.remove(player.getUniqueId());
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "設定をキャンセルしました。");
        }
    }

    private void removeData(Player player, String key) {
        UUID uuid = player.getUniqueId();
        if (sessionData.containsKey(uuid)) {
            sessionData.get(uuid).remove(key);
            player.sendMessage(ChatColor.YELLOW + "設定をリセットしました。");
            SpawnerToolGUI.open(player, player.getInventory().getItemInMainHand(), sessionData.get(uuid));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (waitingForMobName.containsKey(uuid)) {
            event.setCancelled(true);
            String mobName = event.getMessage().trim();
            waitingForMobName.remove(uuid);

            Map<String, String> data = sessionData.computeIfAbsent(uuid, k -> new HashMap<>());
            data.put("mob", mobName);

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(ChatColor.GREEN + "モブ名をセットしました: " + mobName);
                SpawnerToolGUI.open(player, player.getInventory().getItemInMainHand(), sessionData.get(uuid));
            });
        } else if (waitingForSpawnerName.containsKey(uuid)) {
            event.setCancelled(true);
            String spawnerName = event.getMessage().trim();
            waitingForSpawnerName.remove(uuid);

            Map<String, String> data = sessionData.computeIfAbsent(uuid, k -> new HashMap<>());
            data.put("spawner", spawnerName);

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(ChatColor.GREEN + "スポナーベース名をセットしました: " + spawnerName);
                SpawnerToolGUI.open(player, player.getInventory().getItemInMainHand(), sessionData.get(uuid));
            });
        } else if (waitingForGroupName.containsKey(uuid)) {
            event.setCancelled(true);
            String groupName = event.getMessage().trim();
            waitingForGroupName.remove(uuid);

            Map<String, String> data = sessionData.computeIfAbsent(uuid, k -> new HashMap<>());
            data.put("group", groupName);

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(ChatColor.GREEN + "スポナーグループをセットしました: " + groupName);
                SpawnerToolGUI.open(player, player.getInventory().getItemInMainHand(), sessionData.get(uuid));
            });
        } else if (waitingForOption.containsKey(uuid)) {
            event.setCancelled(true);
            String value = event.getMessage().trim();
            String opt = waitingForOption.remove(uuid);

            Map<String, String> data = sessionData.computeIfAbsent(uuid, k -> new HashMap<>());
            data.put("opt." + opt, value);

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(ChatColor.GREEN + "オプション '" + opt + "' をセットしました: " + value);
                SpawnerToolGUI.open(player, player.getInventory().getItemInMainHand(), sessionData.get(uuid));
            });
        }
    }

    private void complete(Player player) {
        UUID uuid = player.getUniqueId();
        Map<String, String> data = sessionData.get(uuid);

        if (data == null || !data.containsKey("mob") || !data.containsKey("spawner")) {
            player.sendMessage(ChatColor.RED + "モブ名とスポナーベース名の両方を設定してください。");
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "アイテムを手に持っていません。");
            return;
        }

        String mob = data.get("mob");
        String spawner = data.get("spawner");
        String group = data.getOrDefault("group", "");

        // 見た目の更新
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "スポナー設置ツール: " + mob);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        
        // PersistentDataContainer にデータを保存
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(KEY_MOB, PersistentDataType.STRING, mob);
        pdc.set(KEY_NAME, PersistentDataType.STRING, spawner);
        if (!group.isEmpty()) {
            pdc.set(KEY_GROUP, PersistentDataType.STRING, group);
        }
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (entry.getKey().startsWith("opt.")) {
                String optKey = entry.getKey().substring(4).toLowerCase();
                NamespacedKey key = new NamespacedKey("lifemoremythicmobs", "spawner_opt_" + optKey);
                pdc.set(key, PersistentDataType.STRING, entry.getValue());
            }
        }
        
        item.setItemMeta(meta);

        sessionData.remove(uuid);
        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "スポナー設置ツールが完成しました！");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR) return;
        
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (!item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(KEY_MOB, PersistentDataType.STRING)) return;

        event.setCancelled(true);
        String mob = pdc.get(KEY_MOB, PersistentDataType.STRING);
        String baseName = pdc.get(KEY_NAME, PersistentDataType.STRING);
        String group = pdc.get(KEY_GROUP, PersistentDataType.STRING);

        int count = getNextCount(baseName);
        String finalName = baseName + "_" + count;

        Player player = event.getPlayer();
        player.performCommand("mm spawners create " + finalName + " " + mob);
        
        if (group != null && !group.isEmpty()) {
            player.performCommand("mm spawners set " + finalName + " group " + group);
        }

        for (NamespacedKey key : pdc.getKeys()) {
            String namespace = key.getNamespace();
            String keyStr = key.getKey();
            if (!namespace.equals("lifemoremythicmobs") || !keyStr.startsWith("spawner_opt_")) continue;
            if (key.equals(KEY_MOB) || key.equals(KEY_NAME) || key.equals(KEY_GROUP)) continue;
            
            String optName = keyStr.substring("spawner_opt_".length());
            String optValue = pdc.get(key, PersistentDataType.STRING);
            player.performCommand("mm spawners set " + finalName + " " + optName + " " + optValue);
        }

        if (group != null && !group.isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "スポナーを作成し、グループ '" + group + "' に追加しました: " + finalName + " (" + mob + ")");
        } else {
            player.sendMessage(ChatColor.GREEN + "スポナーを作成しました: " + finalName + " (" + mob + ")");
        }
    }

    private int getNextCount(String baseName) {
        return (int) MythicBukkit.inst().getSpawnerManager().getSpawners().stream()
                .filter(s -> s.getName().startsWith(baseName + "_"))
                .count() + 1;
    }
}
