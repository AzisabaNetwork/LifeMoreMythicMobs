package net.azisaba.lifemoremythicmobs.gui.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GuiHolder implements InventoryHolder {
   private final GuiHolder.Type type;

   public GuiHolder(GuiHolder.Type t) {
      this.type = t;
   }

   public GuiHolder.Type getType() {
      return this.type;
   }

   public Inventory getInventory() {
      return null;
   }

   public enum Type {
      MAIN,
      VALUE;
   }
}
