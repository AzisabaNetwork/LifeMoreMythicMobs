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

    public SpawnerManagerListener(LifeMoreMythicMobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith("スポナー管理") && !title.startsWith(SpawnerManagerGUI.SPAWNER_LIST_TITLE_PREFIX) && !title.startsWith(SpawnerManagerGUI.DETAIL_TITLE_PREFIX)) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getRawSlot();

        if (title.equals(SpawnerManagerGUI.MAIN_TITLE)) {
            if (slot == 2) {
                currentPage.put(player.getUniqueId(), 0);
                SpawnerManagerGUI.openWorldList(player, 0);
            }
            else if (slot == 6) {
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
                SpawnerManagerGUI.openSpawnerList(player, "world", worldName, 0);
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
                SpawnerManagerGUI.openMain(player);
            }
        } 
        else if (title.equals(SpawnerManagerGUI.GROUP_LIST_TITLE)) {
            if (slot < 45) {
                String groupName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                currentFilterType.put(player.getUniqueId(), "group");
                currentFilterValue.put(player.getUniqueId(), groupName);
                currentPage.put(player.getUniqueId(), 0);
                SpawnerManagerGUI.openSpawnerList(player, "group", groupName, 0);
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
                SpawnerManagerGUI.openMain(player);
            }
        } 
        else if (title.startsWith(SpawnerManagerGUI.SPAWNER_LIST_TITLE_PREFIX)) {
            if (slot < 45) {
                String spawnerName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                if (event.getClick() == ClickType.RIGHT) {
                    visualizeSpawner(player, spawnerName);
                } else {
                    SpawnerManagerGUI.openDetail(player, spawnerName);
                }
            } else if (slot == 45) {
                int page = currentPage.getOrDefault(player.getUniqueId(), 0) - 1;
                currentPage.put(player.getUniqueId(), page);
                SpawnerManagerGUI.openSpawnerList(player, currentFilterType.get(player.getUniqueId()), currentFilterValue.get(player.getUniqueId()), page);
            } else if (slot == 53) {
                int page = currentPage.getOrDefault(player.getUniqueId(), 0) + 1;
                currentPage.put(player.getUniqueId(), page);
                SpawnerManagerGUI.openSpawnerList(player, currentFilterType.get(player.getUniqueId()), currentFilterValue.get(player.getUniqueId()), page);
            } else if (slot == 49) {
                SpawnerManagerGUI.openMain(player);
            }
        } 
        else if (title.startsWith(SpawnerManagerGUI.DETAIL_TITLE_PREFIX)) {
            String spawnerName = title.substring(SpawnerManagerGUI.DETAIL_TITLE_PREFIX.length());
            if (slot == 11) { // Teleport
                MythicSpawner s = MythicMobs.inst().getSpawnerManager().getSpawnerByName(spawnerName);
                if (s != null) {
                    player.teleport(BukkitAdapter.adapt(s.getLocation()));
                    player.sendMessage(ChatColor.GREEN + "テレポートしました: " + spawnerName);
                }
            } else if (slot == 13) { // Visualize
                visualizeSpawner(player, spawnerName);
            } else if (slot == 15) { // Delete
                player.closeInventory();
                player.performCommand("mm spawners remove " + spawnerName);
                player.sendMessage(ChatColor.RED + "スポナーを削除しました: " + spawnerName);
            } else if (slot == 22) { // Back
                SpawnerManagerGUI.openSpawnerList(player, currentFilterType.get(player.getUniqueId()), currentFilterValue.get(player.getUniqueId()), currentPage.getOrDefault(player.getUniqueId(), 0));
            }
        }
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
        Location loc = BukkitAdapter.adapt(aloc).add(0.5, 0, 0.5);
        
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
}
