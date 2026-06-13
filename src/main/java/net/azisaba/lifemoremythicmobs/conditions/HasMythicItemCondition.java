package net.azisaba.lifemoremythicmobs.conditions;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.items.ItemManager;
import io.lumine.mythic.items.MythicItem;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.util.jnbt.CompoundTag;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HasMythicItemCondition extends SkillCondition {
   private final String itemName;
   private final int amount;

   public HasMythicItemCondition(MythicLineConfig config) {
      super(config.getLine());
      this.itemName = config.getString("item", "");
      this.amount = config.getInteger("amount", 1);
   }

   public boolean check(AbstractEntity entity) {
      if (!entity.isPlayer()) {
         return false;
      }

      Player player = (Player)BukkitAdapter.adapt(entity);
      ItemManager itemManager = MythicBukkit.inst().getItemManager();
      MythicItem mythicItem = (MythicItem)itemManager.getItem(this.itemName).orElse(null);
      if (mythicItem == null) {
         return false;
      }

      int count = 0;
      ItemStack[] var9;
      int var8 = (var9 = player.getInventory().getContents()).length;

      for (int var7 = 0; var7 < var8; var7++) {
         ItemStack item = var9[var7];
         if (item != null) {
            CompoundTag tag = MythicBukkit.inst().getVolatileCodeHandler().getItemHandler().getNBTData(item);
            if (tag != null) {
               String mythicType = tag.getString("MYTHIC_TYPE");
               if (mythicType != null && mythicType.equalsIgnoreCase(this.itemName)) {
                  count += item.getAmount();
                  if (count >= this.amount) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }
}
