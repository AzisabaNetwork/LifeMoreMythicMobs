package net.azisaba.lifemoremythicmobs.conditions;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import java.util.Collection;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HasAttributeCondition extends SkillCondition implements IEntityCondition {
   private final String slot;
   private final String attributeSlot;
   private final Attribute attribute;

   public HasAttributeCondition(MythicLineConfig config) {
      super(config.getLine());
      this.slot = config.getString(new String[]{"slot", "s"}, "HEAD", new String[0]).toUpperCase();
      this.attributeSlot = config.getString(new String[]{"attributeslot", "as"}, "HEAD", new String[0]).toUpperCase();
      this.attribute = Attribute.valueOf(config.getString(new String[]{"attribute", "a"}, "GENERIC_ARMOR", new String[0]).toUpperCase());
   }

   public boolean check(AbstractEntity entity) {
      if (!(entity.getBukkitEntity() instanceof Player)) {
         return false;
      }

      Player player = (Player)entity.getBukkitEntity();
      ItemStack item = this.getItemFromSlot(player, this.slot);
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         if (meta == null) {
            return false;
         }

         EquipmentSlot slotEnum = this.getEquipmentSlot(this.attributeSlot);
         Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(this.attribute);
         if (modifiers == null) {
            return false;
         }

         for (AttributeModifier mod : modifiers) {
            if (mod.getSlot() == slotEnum) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private ItemStack getItemFromSlot(Player player, String slot) {
      if ("HEAD".equals(slot)) {
         return player.getInventory().getHelmet();
      } else if ("CHEST".equals(slot)) {
         return player.getInventory().getChestplate();
      } else if ("LEGS".equals(slot)) {
         return player.getInventory().getLeggings();
      } else if ("FEET".equals(slot)) {
         return player.getInventory().getBoots();
      } else if ("MAINHAND".equals(slot)) {
         return player.getInventory().getItemInMainHand();
      } else {
         return "OFFHAND".equals(slot) ? player.getInventory().getItemInOffHand() : null;
      }
   }

   private EquipmentSlot getEquipmentSlot(String slot) {
      if ("HEAD".equals(slot)) {
         return EquipmentSlot.HEAD;
      } else if ("CHEST".equals(slot)) {
         return EquipmentSlot.CHEST;
      } else if ("LEGS".equals(slot)) {
         return EquipmentSlot.LEGS;
      } else if ("FEET".equals(slot)) {
         return EquipmentSlot.FEET;
      } else if ("MAINHAND".equals(slot)) {
         return EquipmentSlot.HAND;
      } else {
         return "OFFHAND".equals(slot) ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND;
      }
   }
}
