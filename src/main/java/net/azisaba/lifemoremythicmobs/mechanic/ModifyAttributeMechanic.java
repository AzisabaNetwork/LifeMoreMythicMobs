package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ModifyAttributeMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final PlaceholderString slot;
   private final PlaceholderString attributeSlot;
   private final Attribute attribute;
   private final PlaceholderString mode;
   private final PlaceholderString rawValue;

   public ModifyAttributeMechanic(SkillExecutor executor, MythicLineConfig config) {
      super(executor, config.getLine(), config);
      this.slot = PlaceholderString.of(config.getString(new String[]{"slot", "s"}, "HEAD").toUpperCase());
      this.attributeSlot = PlaceholderString.of(config.getString(new String[]{"attributeSlot", "as"}, "HEAD").toUpperCase());
      this.attribute = Attribute.valueOf(config.getString(new String[]{"attribute", "a"}, "GENERIC_ARMOR").toUpperCase());
      this.rawValue = PlaceholderString.of(config.getString(new String[]{"value", "v"}, "0.0"));
      this.mode = PlaceholderString.of(config.getString(new String[]{"mode", "m"}, "overwrite").toLowerCase());
   }

   @Override
   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      String slot = this.slot.get(data);
      String attrSlot = this.attributeSlot.get(data);
      String mode = this.mode.get(data);
      if (!(target.getBukkitEntity() instanceof Player)) {
         return SkillResult.FAILURE;
      }

      Player player = (Player)target.getBukkitEntity();
      ItemStack item = this.getItemFromSlot(player, slot);
      if (item != null && item.getType() != Material.AIR) {
         ItemMeta meta = item.getItemMeta();
         if (meta == null) {
            return SkillResult.FAILURE;
         }

         EquipmentSlot equipmentSlot = this.getEquipmentSlot(attrSlot);
         Collection<AttributeModifier> mods = meta.getAttributeModifiers(this.attribute);
         AttributeModifier matched = null;
         Operation baseOp = Operation.ADD_NUMBER;
         double baseAmount = 0.0;
         if (mods != null) {
            for (AttributeModifier mod : mods) {
               if (mod.getSlot() == equipmentSlot) {
                  matched = mod;
                  baseAmount = mod.getAmount();
                  baseOp = mod.getOperation();
                  break;
               }
            }
         }

         String evaluated = this.rawValue.get(data).trim();
         boolean isPercent = evaluated.endsWith("%");
         double parsedValue = isPercent ? this.parsePercent(evaluated) : this.parseRawDouble(evaluated);
         double finalAmount;
         Operation newOp;
         if (mode.equals("add")) {
            if (matched == null) {
               finalAmount = parsedValue;
               newOp = isPercent ? Operation.ADD_SCALAR : Operation.ADD_NUMBER;
            } else {
               finalAmount = baseAmount + parsedValue;
               newOp = baseOp;
            }
         } else if (mode.equals("multiply")) {
            if (matched == null) {
               return SkillResult.FAILURE;
            }

            finalAmount = baseAmount * parsedValue;
            newOp = baseOp;
         } else {
            newOp = isPercent ? Operation.ADD_SCALAR : Operation.ADD_NUMBER;
            finalAmount = parsedValue;
         }

         if (matched != null) {
            meta.removeAttributeModifier(this.attribute, matched);
         }

         if (mode.equals("overwrite") || mode.equals("add") || matched != null) {
            meta.addAttributeModifier(
               this.attribute, new AttributeModifier(UUID.randomUUID(), "mm-" + this.attribute.name(), finalAmount, newOp, equipmentSlot)
            );
            this.updateLore(meta, this.attribute, finalAmount, newOp, equipmentSlot);
         }

         item.setItemMeta(meta);
         this.setItemToSlot(player, slot, item);
         return SkillResult.SUCCESS;
      } else {
         return SkillResult.FAILURE;
      }
   }

   private void updateLore(ItemMeta meta, Attribute attribute, double value, Operation op, EquipmentSlot slot) {
      String displayName = this.getAttributeDisplayName(attribute);
      String sectionTitle = "§7" + this.getSlotDescription(slot);
      String entry = (value >= 0.0 ? "§9" : "§c") + displayName + " " + this.formatAmount(value, op);
      List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
      boolean sectionFound = false;
      boolean entryUpdated = false;

      for (int i = 0; i < lore.size(); i++) {
         if (lore.get(i).equals(sectionTitle)) {
            sectionFound = true;

            for (int j = i + 1; j < lore.size(); j++) {
               String line = lore.get(j);
               if (!line.startsWith("§9") && !line.startsWith("§c")) {
                  break;
               }

               if (line.contains(displayName)) {
                  lore.set(j, entry);
                  entryUpdated = true;
                  break;
               }
            }

            if (!entryUpdated) {
               lore.add(i + 1, entry);
            }
            break;
         }
      }

      if (!sectionFound) {
         lore.add(sectionTitle);
         lore.add(entry);
      }

      meta.setLore(lore);
   }

   private String getSlotDescription(EquipmentSlot slot) {
      if (slot == EquipmentSlot.HAND) {
         return "メインハンドに";
      } else if (slot == EquipmentSlot.OFF_HAND) {
         return "オフハンドに";
      } else if (slot == EquipmentSlot.HEAD) {
         return "頭に装備したとき:";
      } else if (slot == EquipmentSlot.CHEST) {
         return "胴体に装備したとき:";
      } else if (slot == EquipmentSlot.LEGS) {
         return "脚に装備したとき:";
      } else {
         return slot == EquipmentSlot.FEET ? "足に装備したとき:" : "装備したとき:";
      }
   }

   private String getAttributeDisplayName(Attribute attr) {
      if (attr == Attribute.GENERIC_ARMOR) {
         return "防具";
      } else if (attr == Attribute.GENERIC_ARMOR_TOUGHNESS) {
         return "防具強度";
      } else if (attr == Attribute.GENERIC_ATTACK_DAMAGE) {
         return "攻撃力";
      } else if (attr == Attribute.GENERIC_ATTACK_SPEED) {
         return "攻撃速度";
      } else if (attr == Attribute.GENERIC_KNOCKBACK_RESISTANCE) {
         return "ノックバック耐性";
      } else if (attr == Attribute.GENERIC_LUCK) {
         return "幸運";
      } else if (attr == Attribute.GENERIC_MAX_HEALTH) {
         return "最大体力";
      } else {
         return attr == Attribute.GENERIC_MOVEMENT_SPEED ? "移動速度" : attr.name().replace("GENERIC_", "").replace("_", "").toLowerCase();
      }
   }

   private String formatAmount(double amount, Operation op) {
      return op == Operation.ADD_SCALAR ? String.format("+%.0f%%", amount * 100.0) : String.format("+%.1f", amount);
   }

   private double parsePercent(String val) {
      try {
         val = val.trim().replace("%", "");
         return Double.parseDouble(val) / 100.0;
      } catch (Exception e) {
         return 0.0;
      }
   }

   private double parseRawDouble(String input) {
      try {
         return input.endsWith("%") ? this.parsePercent(input) : Double.parseDouble(input);
      } catch (Exception e) {
         return 0.0;
      }
   }

   private ItemStack getItemFromSlot(Player player, String slot) {
      switch (slot) {
         case "OFFHAND":
            return player.getInventory().getItemInOffHand();
         case "FEET":
            return player.getInventory().getBoots();
         case "HEAD":
            return player.getInventory().getHelmet();
         case "LEGS":
            return player.getInventory().getLeggings();
         case "CHEST":
            return player.getInventory().getChestplate();
         case "MAINHAND":
            return player.getInventory().getItemInMainHand();
         default:
            return null;
      }
   }

   private void setItemToSlot(Player player, String slot, ItemStack item) {
      switch (slot) {
         case "OFFHAND":
            player.getInventory().setItemInOffHand(item);
            break;
         case "FEET":
            player.getInventory().setBoots(item);
            break;
         case "HEAD":
            player.getInventory().setHelmet(item);
            break;
         case "LEGS":
            player.getInventory().setLeggings(item);
            break;
         case "CHEST":
            player.getInventory().setChestplate(item);
            break;
         case "MAINHAND":
            player.getInventory().setItemInMainHand(item);
            break;
      }
   }

   private EquipmentSlot getEquipmentSlot(String slot) {
      switch (slot) {
         case "OFFHAND":
            return EquipmentSlot.OFF_HAND;
         case "FEET":
            return EquipmentSlot.FEET;
         case "HEAD":
            return EquipmentSlot.HEAD;
         case "LEGS":
            return EquipmentSlot.LEGS;
         case "CHEST":
            return EquipmentSlot.CHEST;
         case "MAINHAND":
            return EquipmentSlot.HAND;
         default:
            return EquipmentSlot.HAND;
      }
   }
}
