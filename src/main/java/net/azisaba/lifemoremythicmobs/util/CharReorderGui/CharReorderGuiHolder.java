package net.azisaba.lifemoremythicmobs.util.CharReorderGui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class CharReorderGuiHolder implements InventoryHolder {
   private final UUID owner;

   public CharReorderGuiHolder(UUID owner) {
      this.owner = owner;
   }

   public UUID getOwner() {
      return this.owner;
   }

   public Inventory getInventory() {
      return null;
   }
}

