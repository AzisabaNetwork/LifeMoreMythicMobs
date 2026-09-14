package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.SkillCondition;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HasMythicItemCondition extends SkillCondition implements IEntityCondition {
   private final String itemName;
   private final int amount;

   public HasMythicItemCondition(MythicLineConfig config) {
      super(config.getLine());
      this.itemName = config.getString(new String[]{"item", "i", "mmitem", "id", "mmid"}, "");
      this.amount = config.getInteger(new String[]{"amount", "a"}, 1);
   }

   @Override
   public boolean check(AbstractEntity entity) {
      if (!entity.isPlayer() || this.itemName.isEmpty()) {
         return false;
      }

      Player player = (Player)BukkitAdapter.adapt(entity);
      int count = 0;

      for (ItemStack item : player.getInventory().getContents()) {
         if (item != null && !item.getType().isAir()) {
            String mythicType = ItemUtil.getMythicType(item);
            if (mythicType != null && mythicType.equalsIgnoreCase(this.itemName)) {
               count += item.getAmount();
               if (count >= this.amount) {
                  return true;
               }
            }
         }
      }

      return false;
   }
}



