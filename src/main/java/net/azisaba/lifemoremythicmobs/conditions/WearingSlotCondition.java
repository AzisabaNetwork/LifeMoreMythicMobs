package net.azisaba.lifemoremythicmobs.conditions;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.bukkit.BukkitItemStack;
import io.lumine.mythic.drops.EquipSlot;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.items.MythicItem;
import io.lumine.mythic.logging.MythicLogger;
import io.lumine.mythic.logging.MythicLogger.DebugLevel;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.util.annotations.MythicField;
import io.lumine.mythic.utils.version.MinecraftVersions;
import io.lumine.mythic.utils.version.ServerVersion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WearingSlotCondition extends SkillCondition implements IEntityCondition {
   @MythicField(name = "armorslot", aliases = {"slot", "s"}, description = "The equip slot to check (HEAD/CHEST/LEGS/FEET/HAND/OFFHAND)")
   private EquipSlot slot;
   @MythicField(name = "material", aliases = {"mmitem", "m"}, description = "A material or MythicItem name to check for")
   private String itemNamesRaw;
   private final List<ItemStack> items = new ArrayList<>();
   @MythicField(name = "checklore", aliases = "cl", description = "Whether to strictly match item lore")
   private boolean checklore;
   @MythicField(
      name = "inventoryslot",
      aliases = {"invslot", "index"},
      description = "If set, checks a specific inventory slot: 1-9 = hotbar, 10-36 = main inventory (1-27)"
   )
   private int inventorySlot;

   public WearingSlotCondition(String line, MythicLineConfig mlc) {
      super(line);
      String s = mlc.getString(new String[]{"armorslot", "slot", "s"}, "NONE", new String[0]).toUpperCase();
      this.itemNamesRaw = mlc.getString(
         new String[]{"material", "mat", "m", "mythicmobsitem", "mmitem", "mmi", "item"}, "DIRT", new String[]{this.conditionVar}
      );
      this.checklore = mlc.getBoolean(new String[]{"checklore", "cl"}, false);
      this.inventorySlot = mlc.getInteger(new String[]{"inventoryslot", "invslot", "index"}, -1);
      this.slot = EquipSlot.of(s);

      for (String token : Arrays.stream(this.itemNamesRaw.split(",")).map(String::trim).filter(t -> !t.isEmpty()).collect(Collectors.toList())) {
         Optional<MythicItem> maybeItem = MythicBukkit.inst().getItemManager().getItem(token);
         if (maybeItem.isPresent()) {
            ItemStack it = ((BukkitItemStack)maybeItem.get().generateItemStack(1)).build();
            this.items.add(it);
         } else {
            try {
               this.items.add(new ItemStack(Material.valueOf(token.toUpperCase(Locale.ROOT))));
            } catch (Exception ex) {
               MythicLogger.errorConditionConfig(this, mlc, "Item Type not found (or not supported by this version of MythicMobs): " + token);
            }
         }
      }

      if (this.items.isEmpty()) {
         this.items.add(new ItemStack(Material.DIRT));
         MythicLogger.errorConditionConfig(this, mlc, "No valid items/materials found, defaulting to DIRT. (arg=" + this.itemNamesRaw + ")");
      }
   }

   public boolean check(AbstractEntity e) {
      MythicLogger.debug(DebugLevel.CONDITION, "Checking WEARINGSLOT Condition...", new Object[0]);
      if (!e.isLiving()) {
         MythicLogger.debug(DebugLevel.CONDITION, "! Entity is not living, returning false", new Object[0]);
         return false;
      }

      LivingEntity le = (LivingEntity)e.getBukkitEntity();
      ItemStack slotItem;
      if (this.inventorySlot > 0) {
         if (!(le instanceof Player)) {
            MythicLogger.debug(DebugLevel.CONDITION, "! inventoryslot is set but entity is not a player, returning false", new Object[0]);
            return false;
         }

         Player p = (Player)le;
         int idx = this.inventorySlot - 1;
         if (idx < 0 || idx > 35) {
            MythicLogger.debug(DebugLevel.CONDITION, "! inventoryslot out of range (1-36), returning false", new Object[0]);
            return false;
         }

         slotItem = p.getInventory().getItem(idx);
      } else {
         switch (this.slot) {
            case HEAD:
               slotItem = le.getEquipment().getHelmet();
               break;
            case CHEST:
               slotItem = le.getEquipment().getChestplate();
               break;
            case LEGS:
               slotItem = le.getEquipment().getLeggings();
               break;
            case FEET:
               slotItem = le.getEquipment().getBoots();
               break;
            case HAND:
               slotItem = le.getEquipment().getItemInMainHand();
               break;
            case OFFHAND:
               slotItem = le.getEquipment().getItemInOffHand();
               break;
            case NONE:
            default:
               MythicLogger.debug(DebugLevel.CONDITION, "! Invalid or NONE slot used and no inventoryslot set, returning false", new Object[0]);
               return false;
         }
      }

      if (slotItem != null && slotItem.getType() != Material.AIR) {
         for (ItemStack candidate : this.items) {
            if (this.matches(candidate, slotItem)) {
               MythicLogger.debug(DebugLevel.CONDITION, "+ Item matches (OR), returning true", new Object[0]);
               return true;
            }
         }

         MythicLogger.debug(DebugLevel.CONDITION, "! No candidates matched, returning false", new Object[0]);
         return false;
      } else {
         MythicLogger.debug(DebugLevel.CONDITION, "! Slot item was null or AIR, returning false", new Object[0]);
         return false;
      }
   }

   private boolean matches(ItemStack expected, ItemStack actual) {
      if (!expected.getType().equals(actual.getType())) {
         MythicLogger.debug(DebugLevel.CONDITION, "! Type doesn't match", new Object[0]);
         return false;
      }

      if (expected.hasItemMeta() != actual.hasItemMeta()) {
         MythicLogger.debug(DebugLevel.CONDITION, "! Meta state doesn't match", new Object[0]);
         return false;
      }

      if (expected.hasItemMeta()) {
         ItemMeta meta = expected.getItemMeta();
         ItemMeta meta2 = actual.getItemMeta();
         if (meta.hasDisplayName() != meta2.hasDisplayName()) {
            MythicLogger.debug(DebugLevel.CONDITION, "! Display doesn't match", new Object[0]);
            return false;
         }

         if (meta.hasDisplayName() && !meta.getDisplayName().equals(meta2.getDisplayName())) {
            MythicLogger.debug(DebugLevel.CONDITION, "! Display doesn't match", new Object[0]);
            return false;
         }

         if (ServerVersion.isAfterOrEq(MinecraftVersions.v1_14)) {
            if (meta.hasCustomModelData() != meta2.hasCustomModelData()) {
               MythicLogger.debug(DebugLevel.CONDITION, "! CustomModelData doesn't match", new Object[0]);
               return false;
            }

            if (meta.hasCustomModelData() && meta.getCustomModelData() != meta2.getCustomModelData()) {
               MythicLogger.debug(DebugLevel.CONDITION, "! CustomModelData doesn't match", new Object[0]);
               return false;
            }
         }

         if (this.checklore) {
            if (meta.hasLore() != meta2.hasLore()) {
               MythicLogger.debug(DebugLevel.CONDITION, "! Lore presence doesn't match", new Object[0]);
               return false;
            }

            if (meta.hasLore() && !meta.getLore().equals(meta2.getLore())) {
               MythicLogger.debug(DebugLevel.CONDITION, "! Lore doesn't match", new Object[0]);
               return false;
            }
         }
      }

      return true;
   }
}
