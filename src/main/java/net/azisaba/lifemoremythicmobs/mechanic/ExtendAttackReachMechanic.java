package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.items.MythicItem;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.util.jnbt.CompoundTag;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class ExtendAttackReachMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final double reachDistance;
   private final int durationTicks;

   public ExtendAttackReachMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.reachDistance = config.getDouble("reach", 5.0);
      this.durationTicks = config.getInteger("duration", 100);
   }

   public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity target) {
      if (!(target.getBukkitEntity() instanceof Player)) {
         return SkillResult.FAILURE;
      }

      Player player = (Player)target.getBukkitEntity();
      this.extendAttackReach(player, this.reachDistance, this.durationTicks);
      return SkillResult.SUCCESS;
   }

   private void extendAttackReach(Player player, double reach, int duration) {
      BukkitTask task = JavaPlugin.getPlugin(LifeMoreMythicMobs.class)
         .getServer()
         .getScheduler()
         .runTaskTimer(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), () -> this.performReachCheck(player, reach), 0L, 1L);
      JavaPlugin.getPlugin(LifeMoreMythicMobs.class).getServer().getScheduler().runTaskLater(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), () -> task.cancel(), duration);
   }

   private void performReachCheck(Player player, double reach) {
      if (player != null && player.isOnline()) {
         double baseDamage = this.getCustomWeaponDamage(player.getInventory().getItemInMainHand());
         double totalDamage = this.applyEnchantmentsAndEffects(player, baseDamage);
         Location eyeLocation = player.getEyeLocation();
         Vector direction = eyeLocation.getDirection();
         RayTraceResult result = player.getWorld()
            .rayTraceEntities(eyeLocation, direction, reach, entity -> entity instanceof LivingEntity && entity != player);
         if (result != null && result.getHitEntity() != null) {
            Entity hitEntity = result.getHitEntity();
            if (hitEntity instanceof LivingEntity) {
               LivingEntity target = (LivingEntity)hitEntity;
               target.damage(totalDamage, player);
            }
         }
      }
   }

   private double getCustomWeaponDamage(ItemStack weapon) {
      if (weapon != null && !weapon.getType().isAir()) {
         CompoundTag tag = MythicBukkit.inst().getVolatileCodeHandler().getItemHandler().getNBTData(weapon);
         if (tag != null && tag.containsKey("MYTHIC_TYPE")) {
            String weaponName = tag.getString("MYTHIC_TYPE");
            MythicItem mythicWeapon = (MythicItem)MythicBukkit.inst().getItemManager().getItem(weaponName).orElse(null);
            if (mythicWeapon != null) {
               String damageStr = mythicWeapon.getConfig().getString("Attributes.Mainhand.Damage");
               if (damageStr != null && !damageStr.isEmpty()) {
                  try {
                     double customDamage = Double.parseDouble(damageStr.replace("%", "").trim());
                     if (customDamage > 0.0) {
                        return customDamage;
                     }
                  } catch (NumberFormatException e) {
                     return getVanillaWeaponDamage(weapon);
                  }
               }
            }
         }

         return getVanillaWeaponDamage(weapon);
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
         finalDamage += this.calculateSharpnessBonus(level, baseDamage);
      }

      if (player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
         int amplifier = player.getPotionEffect(PotionEffectType.INCREASE_DAMAGE).getAmplifier();
         finalDamage += baseDamage * (1.3 * (amplifier + 1));
      }

      if (this.isCriticalHit(player)) {
         finalDamage *= 1.5;
      }

      return finalDamage;
   }

   private double calculateSharpnessBonus(int level, double baseDamage) {
      return 1.0 + 0.5 * level + Math.max(0.0, (level - 1) * 0.5);
   }

   private boolean isCriticalHit(Player player) {
      return player.getFallDistance() > 0.0F && !player.isOnGround() && !player.isInsideVehicle() && !player.hasPotionEffect(PotionEffectType.BLINDNESS);
   }
}
