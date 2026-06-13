package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

public class RotteryRewardRedeeGuiMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final LifeMoreMythicMobs plugin = JavaPlugin.getPlugin(LifeMoreMythicMobs.class);
   private final PlaceholderInt ticksA;
   private final PlaceholderInt ticksB;
   private static final String TITLE = ChatColor.translateAlternateColorCodes('&', "&6宝くじを換金ちゅう");
   private static final int SLOT_A = 11;
   private static final int SLOT_B = 12;
   private static final int SLOT_C = 13;
   private static final int SLOT_D = 14;
   private static final int SLOT_E = 15;
   private static final Map<UUID, RotteryRewardRedeeGuiMechanic.Session> ACTIVE = new ConcurrentHashMap<>();
   private static volatile boolean LISTENER_REGISTERED = false;

   public RotteryRewardRedeeGuiMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.ticksA = config.getPlaceholderInteger("a", 60);
      this.ticksB = config.getPlaceholderInteger("b", 40);
      ensureListeners();
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      if (!target.isPlayer()) {
         return SkillResult.FAILURE;
      }

      Player p = (Player)BukkitAdapter.adapt(target);
      if (p != null && p.isOnline()) {
         int a = Math.max(0, this.ticksA.get(data, target));
         int b = Math.max(0, this.ticksB.get(data, target));
         int extra = 20;
         int total = a + b + 20;
         RotteryRewardRedeeGuiMechanic.Session old = ACTIVE.remove(p.getUniqueId());
         if (old != null) {
            old.cancel();
         }

         Inventory inv = Bukkit.createInventory(p, 27, TITLE);
         fillAllGray(inv);
         inv.setItem(11, ironWithModel(145));
         inv.setItem(12, whitePaneWithModel(1));
         p.openInventory(inv);
         RotteryRewardRedeeGuiMechanic.Session session = new RotteryRewardRedeeGuiMechanic.Session(p.getUniqueId(), inv);
         ACTIVE.put(p.getUniqueId(), session);
         if (a > 0) {
            session.tasks.add(Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
               if (session.isActive()) {
                  inv.setItem(13, ironWithModel(170));
                  inv.setItem(14, whitePaneWithModel(1));
                  reopenIfNeeded(session, p, inv);
               }
            }, a));
         } else {
            inv.setItem(13, ironWithModel(170));
            inv.setItem(14, whitePaneWithModel(1));
         }

         long showEAt = a + b;
         if (showEAt > 0L) {
            session.tasks.add(Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
               if (session.isActive()) {
                  inv.setItem(15, ironWithModel(83));
                  reopenIfNeeded(session, p, inv);
               }
            }, showEAt));
         } else {
            inv.setItem(15, ironWithModel(83));
         }

         session.tasks.add(Bukkit.getScheduler().runTaskLater(this.plugin, () -> endSession(p.getUniqueId(), false), total));
         return SkillResult.SUCCESS;
      } else {
         return SkillResult.FAILURE;
      }
   }

   private static String safeName(Player p) {
      return p != null ? p.getName() + "(" + p.getUniqueId().toString().substring(0, 8) + ")" : "null";
   }

   private static void fillAllGray(Inventory inv) {
      ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
      ItemMeta meta = filler.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(" ");
         filler.setItemMeta(meta);
      }

      for (int i = 0; i < inv.getSize(); i++) {
         inv.setItem(i, filler);
      }
   }

   private static ItemStack ironWithModel(int cmd) {
      ItemStack it = new ItemStack(Material.IRON_INGOT);
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.setCustomModelData(cmd);
         meta.setDisplayName(ChatColor.RESET + "");
         it.setItemMeta(meta);
      }

      return it;
   }

   private static ItemStack whitePaneWithModel(int cmd) {
      ItemStack it = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.setCustomModelData(cmd);
         meta.setDisplayName(ChatColor.RESET + "");
         it.setItemMeta(meta);
      }

      return it;
   }

   private static void ensureListeners() {
      if (!LISTENER_REGISTERED) {
         LISTENER_REGISTERED = true;
         final LifeMoreMythicMobs plugin = JavaPlugin.getPlugin(LifeMoreMythicMobs.class);
         Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
            public void onClick(InventoryClickEvent e) {
               if (e.getWhoClicked() instanceof Player) {
                  Player p = (Player)e.getWhoClicked();
                  if (RotteryRewardRedeeGuiMechanic.ACTIVE.containsKey(p.getUniqueId())) {
                     e.setCancelled(true);
                  }
               }
            }

            @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
            public void onDrag(InventoryDragEvent e) {
               if (e.getWhoClicked() instanceof Player) {
                  Player p = (Player)e.getWhoClicked();
                  if (RotteryRewardRedeeGuiMechanic.ACTIVE.containsKey(p.getUniqueId())) {
                     e.setCancelled(true);
                  }
               }
            }

            @EventHandler(priority = EventPriority.MONITOR)
            public void onClose(InventoryCloseEvent e) {
               if (e.getPlayer() instanceof Player) {
                  Player p = (Player)e.getPlayer();
                  RotteryRewardRedeeGuiMechanic.Session s = RotteryRewardRedeeGuiMechanic.ACTIVE.get(p.getUniqueId());
                  if (s != null) {
                     Bukkit.getScheduler().runTask(plugin, () -> {
                        RotteryRewardRedeeGuiMechanic.Session s2 = RotteryRewardRedeeGuiMechanic.ACTIVE.get(p.getUniqueId());
                        if (s2 != null && s2.isActive()) {
                           p.openInventory(s2.inv);
                        }
                     });
                  }
               }
            }

            @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
            public void onDrop(PlayerDropItemEvent e) {
               if (RotteryRewardRedeeGuiMechanic.ACTIVE.containsKey(e.getPlayer().getUniqueId())) {
                  e.setCancelled(true);
               }
            }

            @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
            public void onSwap(PlayerSwapHandItemsEvent e) {
               if (RotteryRewardRedeeGuiMechanic.ACTIVE.containsKey(e.getPlayer().getUniqueId())) {
                  e.setCancelled(true);
               }
            }

            @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
            public void onHold(PlayerItemHeldEvent e) {
               if (RotteryRewardRedeeGuiMechanic.ACTIVE.containsKey(e.getPlayer().getUniqueId())) {
                  e.setCancelled(true);
               }
            }

            @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
            public void onInteract(PlayerInteractEvent e) {
               if (RotteryRewardRedeeGuiMechanic.ACTIVE.containsKey(e.getPlayer().getUniqueId())) {
                  e.setCancelled(true);
               }
            }

            @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
            public void onCommand(PlayerCommandPreprocessEvent e) {
               if (RotteryRewardRedeeGuiMechanic.ACTIVE.containsKey(e.getPlayer().getUniqueId())) {
                  e.setCancelled(true);
               }
            }

            @EventHandler(priority = EventPriority.MONITOR)
            public void onQuit(PlayerQuitEvent e) {
               UUID id = e.getPlayer().getUniqueId();
               RotteryRewardRedeeGuiMechanic.Session s = RotteryRewardRedeeGuiMechanic.ACTIVE.remove(id);
               if (s != null) {
                  s.cancel();
               }
            }

            @EventHandler(priority = EventPriority.MONITOR)
            public void onDeath(PlayerDeathEvent e) {
               UUID id = e.getEntity().getUniqueId();
               RotteryRewardRedeeGuiMechanic.Session s = RotteryRewardRedeeGuiMechanic.ACTIVE.remove(id);
               if (s != null) {
                  s.cancel();
               }
            }
         }, plugin);
      }
   }

   private static void reopenIfNeeded(RotteryRewardRedeeGuiMechanic.Session s, Player p, Inventory inv) {
      if (p.getOpenInventory() == null || !Objects.equals(p.getOpenInventory().getTopInventory(), inv)) {
         p.openInventory(inv);
      }
   }

   private static void endSession(UUID id, boolean forceCloseSilent) {
      RotteryRewardRedeeGuiMechanic.Session s = ACTIVE.remove(id);
      if (s != null) {
         s.cancel();
         Player p = Bukkit.getPlayer(id);
         if (p != null && p.isOnline()) {
            if (!forceCloseSilent) {
               p.closeInventory();
            } else {
               p.closeInventory();
            }
         }
      }
   }

   private static final class Session {
      final UUID playerId;
      final Inventory inv;
      final List<BukkitTask> tasks = new ArrayList<>();
      volatile boolean active = true;

      Session(UUID playerId, Inventory inv) {
         this.playerId = playerId;
         this.inv = inv;
      }

      boolean isActive() {
         return this.active;
      }

      void cancel() {
         this.active = false;

         for (BukkitTask t : this.tasks) {
            try {
               t.cancel();
            } catch (Throwable var4) {
            }
         }

         this.tasks.clear();
      }

      void log(String msg) {
         Player p = Bukkit.getPlayer(this.playerId);
         String who = (p != null ? p.getName() : "offline") + "/" + this.playerId.toString().substring(0, 8);
         IgaDebugLogger.log("RedeeGUI", "[" + who + "] " + msg);
      }
   }
}
