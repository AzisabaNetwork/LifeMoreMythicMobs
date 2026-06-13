package net.azisaba.lifemoremythicmobs.listener;

import net.azisaba.lifemoremythicmobs.util.EquipLockManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

public class EquipLockListener implements Listener {
   @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         if (EquipLockManager.getInstance().isLocked(player)) {
            if (event.getClick() == ClickType.NUMBER_KEY) {
               int hotbar = event.getHotbarButton();
               if (hotbar >= 0 && hotbar <= 8) {
                  event.setCancelled(true);
                  return;
               }
            }

            if (event.getSlotType() == SlotType.ARMOR) {
               event.setCancelled(true);
            } else {
               Inventory clicked = event.getClickedInventory();
               if (clicked instanceof PlayerInventory) {
                  PlayerInventory inv = (PlayerInventory)clicked;
                  int slot = event.getSlot();
                  if (slot >= 0 && slot <= 8) {
                     event.setCancelled(true);
                     return;
                  }

                  int offhandSlot = 40;
                  if (slot == offhandSlot) {
                     event.setCancelled(true);
                     return;
                  }
               }
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
   public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
      Player player = event.getPlayer();
      if (EquipLockManager.getInstance().isLocked(player)) {
         event.setCancelled(true);
      }
   }

   @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
   public void onDropItem(PlayerDropItemEvent event) {
      Player player = event.getPlayer();
      if (EquipLockManager.getInstance().isLocked(player)) {
         event.setCancelled(true);
      }
   }
}
