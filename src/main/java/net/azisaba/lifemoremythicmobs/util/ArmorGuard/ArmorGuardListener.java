package net.azisaba.lifemoremythicmobs.util.ArmorGuard;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class ArmorGuardListener implements Listener {
   private final ArmorAttributeGuard guard;

   public ArmorGuardListener(ArmorAttributeGuard guard) {
      this.guard = guard;
   }

   @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
   public void onJoin(PlayerJoinEvent e) {
      this.guard.scheduleApply(e.getPlayer());
   }

   @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
   public void onHeld(PlayerItemHeldEvent e) {
      this.guard.scheduleApply(e.getPlayer());
   }

   @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
   public void onSwap(PlayerSwapHandItemsEvent e) {
      this.guard.scheduleApply(e.getPlayer());
   }

   @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
   public void onInvClick(InventoryClickEvent e) {
      if (e.getWhoClicked() instanceof Player) {
         this.guard.scheduleApply((Player)e.getWhoClicked());
      }
   }

   @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
   public void onEquip(PlayerArmorStandManipulateEvent e) {
      this.guard.scheduleApply(e.getPlayer());
   }
}
