package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.TickCounter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.items.MythicItem;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.util.jnbt.CompoundTag;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class ExtendReachBuffMechanic extends SkillMechanic implements ITargetedEntitySkill, Listener {
   private final PlaceholderDouble reachDistance;
   private final int durationTicks;
   private final boolean refreshDuration;
   private final String onTickSkillName;
   private final int tickInterval;
   private final Map<Player, Long> auraEndTime = new HashMap<>();
   private final Set<Player> activePlayers = new HashSet<>();
   private final Map<Player, Long> lastAttackTick = new HashMap<>();
   private final Map<Player, BukkitTask> tickTasks = new HashMap<>();
   private final Map<Player, Double> playerReachDistance = new HashMap<>();

   public ExtendReachBuffMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.reachDistance = PlaceholderDouble.of(config.getString(new String[]{"reach", "r"}));
      this.durationTicks = config.getInteger(new String[]{"duration", "d"}, 100);
      this.refreshDuration = config.getBoolean(new String[]{"refreshDuration", "rd"}, false);
      this.onTickSkillName = config.getString(new String[]{"onTickSkill", "onTick", "oT", "ots", "ot"}, null, new String[0]);
      this.tickInterval = config.getInteger(new String[]{"interval", "in", "i"}, 20);
   }

   public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity target) {
      if (!(target.getBukkitEntity() instanceof Player)) {
         return SkillResult.FAILURE;
      }

      Player player = (Player)target.getBukkitEntity();
      double evaluatedReach = this.reachDistance.get(skillMetadata);
      this.playerReachDistance.put(player, evaluatedReach);
      this.activateAura(player);
      return SkillResult.SUCCESS;
   }

   private void activateAura(Player player) {
      long now = System.currentTimeMillis();
      long newEnd = now + this.durationTicks * 50L;
      if (this.activePlayers.contains(player)) {
         if (this.refreshDuration) {
            this.auraEndTime.put(player, newEnd);
         }
      } else {
         this.activePlayers.add(player);
         this.auraEndTime.put(player, newEnd);
         if (this.onTickSkillName != null && !this.onTickSkillName.isEmpty()) {
            BukkitTask tick = Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), () -> {
               if (this.activePlayers.contains(player)) {
                  if (System.currentTimeMillis() > this.auraEndTime.getOrDefault(player, 0L)) {
                     this.deactivateAura(player);
                  } else {
                     MythicBukkit.inst().getAPIHelper().castSkill(player, this.onTickSkillName, player.getLocation(), List.of(player), null, 1.0F);
                  }
               }
            }, 0L, this.tickInterval);
            this.tickTasks.put(player, tick);
         }
      }
   }

   private void deactivateAura(Player player) {
      this.activePlayers.remove(player);
      this.auraEndTime.remove(player);
      BukkitTask tick = this.tickTasks.remove(player);
      if (tick != null) {
         tick.cancel();
      }

      this.lastAttackTick.remove(player);
      this.playerReachDistance.remove(player);
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onPlayerSwing(PlayerAnimationEvent event) {
      if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
         Player player = event.getPlayer();
         if (this.activePlayers.contains(player)) {
            this.performReachCheck(player);
         }
      }
   }

   private boolean canAttack(Player player) {
      long lastTick = this.lastAttackTick.getOrDefault(player, 0L);
      long now = TickCounter.getCurrentTick();
      long cooldown = this.getAttackCooldownTicks(player);
      return now - lastTick >= cooldown;
   }

   private long getAttackCooldownTicks(Player player) {
      double attackSpeed = this.getTotalAttackSpeed(player);
      if (player.hasPotionEffect(PotionEffectType.FAST_DIGGING)) {
         int amplifier = player.getPotionEffect(PotionEffectType.FAST_DIGGING).getAmplifier();
         attackSpeed *= 1.0 + (amplifier + 1) * 0.1;
      }

      if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
         int amplifier = player.getPotionEffect(PotionEffectType.SLOW_DIGGING).getAmplifier();
         attackSpeed *= 1.0 - (amplifier + 1) * 0.1;
      }

      return Math.max(1L, Math.round(19.0 / attackSpeed));
   }

   private double getTotalAttackSpeed(Player player) {
      ItemStack mainHand = player.getInventory().getItemInMainHand();
      double base = this.getVanillaAttackSpeed(mainHand);
      double mainBonus = this.getMythicAttribute(mainHand, "AttackSpeed", "Mainhand");
      double offHandBonus = this.getMythicAttribute(player.getInventory().getItemInOffHand(), "AttackSpeed", "OffHand");
      double armorBonus = 0.0;
      ItemStack[] var14;
      int amplifier = (var14 = player.getInventory().getArmorContents()).length;

      for (int var12 = 0; var12 < amplifier; var12++) {
         ItemStack armor = var14[var12];
         if (armor != null && armor.getType() != Material.AIR) {
            String slot = this.getArmorSlot(armor, player);
            if (!slot.isEmpty()) {
               armorBonus += this.getMythicAttribute(armor, "AttackSpeed", slot);
            }
         }
      }

      double totalSpeed = base * (1.0 + mainBonus + offHandBonus + armorBonus);
      if (player.hasPotionEffect(PotionEffectType.FAST_DIGGING)) {
         amplifier = player.getPotionEffect(PotionEffectType.FAST_DIGGING).getAmplifier();
         totalSpeed *= 1.0 + (amplifier + 1) * 0.1;
      }

      if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
         amplifier = player.getPotionEffect(PotionEffectType.SLOW_DIGGING).getAmplifier();
         totalSpeed *= 1.0 - (amplifier + 1) * 0.1;
      }

      return totalSpeed;
   }

   private double getMythicAttribute(ItemStack item, String attribute, String slot) {
      if (item != null && !item.getType().isAir()) {
         CompoundTag tag = MythicBukkit.inst().getVolatileCodeHandler().getItemHandler().getNBTData(item);
         if (tag != null && tag.containsKey("MYTHIC_TYPE")) {
            String itemName = tag.getString("MYTHIC_TYPE");
            MythicItem mythicItem = (MythicItem)MythicBukkit.inst().getItemManager().getItem(itemName).orElse(null);
            if (mythicItem == null) {
               return 0.0;
            }

            String attributesSection = null;

            for (String topKey : mythicItem.getConfig().getKeys("")) {
               if (topKey.equalsIgnoreCase("Attributes")) {
                  attributesSection = topKey;
                  break;
               }
            }

            if (attributesSection == null) {
               return 0.0;
            }

            String slotSection = null;

            for (String slotKey : mythicItem.getConfig().getKeys(attributesSection)) {
               if (slotKey.equalsIgnoreCase(slot)) {
                  slotSection = slotKey;
                  break;
               }
            }

            if (slotSection == null) {
               return 0.0;
            }

            String valueKey = null;
            String pathToSlot = attributesSection + "." + slotSection;

            for (String attrKey : mythicItem.getConfig().getKeys(pathToSlot)) {
               if (attrKey.equalsIgnoreCase(attribute)) {
                  valueKey = attrKey;
                  break;
               }
            }

            if (valueKey == null) {
               return 0.0;
            }

            String finalPath = pathToSlot + "." + valueKey;
            String valueStr = mythicItem.getConfig().getString(finalPath);
            if (valueStr != null && !valueStr.isEmpty()) {
               try {
                  return valueStr.contains("%") ? Double.parseDouble(valueStr.replace("%", "").trim()) / 100.0 : Double.parseDouble(valueStr.trim());
               } catch (NumberFormatException e) {
                  return 0.0;
               }
            } else {
               return 0.0;
            }
         } else {
            return 0.0;
         }
      } else {
         return 0.0;
      }
   }

   private String getArmorSlot(ItemStack item, Player player) {
      if (item != null && !item.getType().isAir()) {
         if (player != null) {
            if (item.equals(player.getInventory().getHelmet())) {
               return "Head";
            }

            if (item.equals(player.getInventory().getChestplate())) {
               return "Chest";
            }

            if (item.equals(player.getInventory().getLeggings())) {
               return "Legs";
            }

            if (item.equals(player.getInventory().getBoots())) {
               return "Feet";
            }
         }

         String name = item.getType().toString();
         if (name.contains("HELMET")) {
            return "Head";
         } else if (name.contains("CHESTPLATE")) {
            return "Chest";
         } else if (name.contains("LEGGINGS")) {
            return "Legs";
         } else {
            return name.contains("BOOTS") ? "Feet" : "";
         }
      } else {
         return "";
      }
   }

   private double getVanillaAttackSpeed(ItemStack weapon) {
      switch ($SWITCH_TABLE$org$bukkit$Material()[weapon.getType().ordinal()]) {
         case 268:
            return 1.0;
         case 279:
         case 365:
         case 445:
         case 872:
         case 954:
            return 1.6;
         case 355:
         case 860:
         case 950:
            return 0.8;
         case 430:
            return 0.9;
         case 905:
            return 1.1;
         default:
            return 1.0;
      }
   }

   private void performReachCheck(Player player) {
      Double reach = this.playerReachDistance.get(player);
      if (reach != null && player != null && player.isOnline()) {
         Location eyeLocation = player.getEyeLocation();
         Vector direction = eyeLocation.getDirection();
         RayTraceResult result = player.getWorld()
            .rayTraceEntities(eyeLocation, direction, reach, entity -> entity instanceof LivingEntity && entity != player);
         if (result != null && result.getHitEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity)result.getHitEntity();
            long lastTick = this.lastAttackTick.getOrDefault(player, 0L);
            long currentTick = TickCounter.getCurrentTick();
            double attackSpeed = this.getTotalAttackSpeed(player);
            if (attackSpeed <= 0.0) {
               return;
            }

            long maxCooldown = Math.max(1L, Math.round(19.0 / attackSpeed));
            double elapsed = currentTick - lastTick;
            double charge = Math.min(1.0, elapsed / maxCooldown);
            double rawDamage = this.getTotalCustomDamage(player);
            if (rawDamage <= 0.0) {
               return;
            }

            double scale = 0.2 + 1.0 / (1.0 + Math.exp(-20.0 * (charge - 0.214))) * 0.8;
            double scaledDamage = rawDamage * scale;
            double finalDamage = this.applyEnchantmentsAndEffects(player, scaledDamage);
            target.setMetadata("ExtendReachCustomDamage", new FixedMetadataValue(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), true));
            target.damage(finalDamage, player);
            this.lastAttackTick.put(player, currentTick);
            Bukkit.getScheduler()
               .runTaskLater(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), () -> target.removeMetadata("ExtendReachCustomDamage", JavaPlugin.getPlugin(LifeMoreMythicMobs.class)), 1L);
         }
      }
   }

   private double getTotalCustomDamage(Player player) {
      ItemStack mainHand = player.getInventory().getItemInMainHand();
      ItemStack offHand = player.getInventory().getItemInOffHand();
      ItemStack[] armorContents = player.getInventory().getArmorContents();
      double baseDamage = this.getCustomWeaponDamage(mainHand);
      double offHandBonus = this.getMythicAttribute(offHand, "Damage", "OffHand");
      double armorBonus = 0.0;
      ItemStack[] var14 = armorContents;
      int attackSpeed = armorContents.length;

      for (int var12 = 0; var12 < attackSpeed; var12++) {
         ItemStack armor = var14[var12];
         if (armor != null && armor.getType() != Material.AIR) {
            String slot = this.getArmorSlot(armor, player);
            if (!slot.isEmpty()) {
               armorBonus += this.getMythicAttribute(armor, "Damage", slot);
            }
         }
      }

      double totalDamage = baseDamage + offHandBonus + armorBonus;
      if (totalDamage <= 0.0) {
         JavaPlugin.getPlugin(LifeMoreMythicMobs.class).getLogger().info("Total attribute damage is zero or negative, skipping damage.");
         return 0.0;
      } else {
         double attackSpeedx = this.getTotalAttackSpeed(player);
         if (attackSpeedx <= 0.0) {
            JavaPlugin.getPlugin(LifeMoreMythicMobs.class).getLogger().info("Attack speed is zero or negative, skipping damage.");
            return 0.0;
         } else {
            return totalDamage;
         }
      }
   }

   private void logItemInfo(ItemStack item, String slot) {
      if (item != null && item.getType() == Material.AIR) {
         CompoundTag tag = MythicBukkit.inst().getVolatileCodeHandler().getItemHandler().getNBTData(item);
         if (tag != null && tag.containsKey("MYTHIC_TYPE")) {
            String var4 = tag.getString("MYTHIC_TYPE");
         }
      }
   }

   @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
   public void onEntityDamage(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player) {
         Player player = (Player)event.getDamager();
         if (!event.getEntity().hasMetadata("ExtendReachCustomDamage")) {
            if (this.activePlayers.contains(player) && event.getCause() == DamageCause.ENTITY_ATTACK) {
               event.setCancelled(true);
            }
         }
      }
   }

   private double getCustomWeaponDamage(ItemStack weapon) {
      if (weapon != null && !weapon.getType().isAir()) {
         double damage = this.getMythicAttribute(weapon, "Damage", "Mainhand");
         return damage > 0.0 ? damage : getVanillaWeaponDamage(weapon);
      } else {
         return 1.0;
      }
   }

   private static double getVanillaWeaponDamage(ItemStack weapon) {
      switch ($SWITCH_TABLE$org$bukkit$Material()[weapon.getType().ordinal()]) {
         case 268:
         case 430:
         case 860:
            return 9.0;
         case 279:
            return 7.0;
         case 355:
         case 950:
            return 7.0;
         case 365:
         case 954:
            return 4.0;
         case 445:
            return 6.0;
         case 872:
            return 5.0;
         default:
            return 1.0;
      }
   }

   private double applyEnchantmentsAndEffects(Player player, double baseDamage) {
      ItemStack weapon = player.getInventory().getItemInMainHand();
      double finalDamage = baseDamage;
      if (weapon.containsEnchantment(Enchantment.DAMAGE_ALL)) {
         int level = weapon.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
         finalDamage += this.calculateSharpnessBonus(level);
      }

      if (player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
         int amplifier = player.getPotionEffect(PotionEffectType.INCREASE_DAMAGE).getAmplifier();
         double strengthBonus = 3.0 * (amplifier + 1);
         finalDamage += strengthBonus;
      }

      if (player.hasPotionEffect(PotionEffectType.WEAKNESS)) {
         int amplifier = player.getPotionEffect(PotionEffectType.WEAKNESS).getAmplifier();
         double weaknessPenalty = 4.0 * (amplifier + 1);
         finalDamage -= weaknessPenalty;
         if (finalDamage < 0.0) {
            finalDamage = 0.0;
         }
      }

      if (this.isCriticalHit(player)) {
         finalDamage *= 1.5;
      }

      return finalDamage;
   }

   private double calculateSharpnessBonus(int level) {
      return 1.0 + 0.5 * level + Math.max(0.0, (level - 1) * 0.5);
   }

   private boolean isCriticalHit(Player player) {
      return player.getFallDistance() > 0.0F && !player.isOnGround() && !player.hasPotionEffect(PotionEffectType.BLINDNESS);
   }
}
