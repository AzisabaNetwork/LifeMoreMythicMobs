package net.azisaba.lifemoremythicmobs.util;

import org.bukkit.attribute.Attribute;

public class AttrLists {
   private AttrLists() {
   }

   public static Attribute[] genericOrder() {
      return new Attribute[]{
         Attribute.GENERIC_MAX_HEALTH,
         Attribute.GENERIC_ATTACK_DAMAGE,
         Attribute.GENERIC_ATTACK_SPEED,
         Attribute.GENERIC_ARMOR,
         Attribute.GENERIC_ARMOR_TOUGHNESS,
         Attribute.GENERIC_MOVEMENT_SPEED,
         Attribute.GENERIC_KNOCKBACK_RESISTANCE,
         Attribute.GENERIC_LUCK
      };
   }
}
