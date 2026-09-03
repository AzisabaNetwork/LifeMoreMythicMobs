package net.azisaba.lifemoremythicmobs.util;

import org.bukkit.attribute.Attribute;

public class AttrLists {
   private AttrLists() {
   }

   public static Attribute[] genericOrder() {
      return new Attribute[]{
         Attribute.MAX_HEALTH,
         Attribute.ATTACK_DAMAGE,
         Attribute.ATTACK_SPEED,
         Attribute.ARMOR,
         Attribute.ARMOR_TOUGHNESS,
         Attribute.MOVEMENT_SPEED,
         Attribute.KNOCKBACK_RESISTANCE,
         Attribute.LUCK
      };
   }
}

