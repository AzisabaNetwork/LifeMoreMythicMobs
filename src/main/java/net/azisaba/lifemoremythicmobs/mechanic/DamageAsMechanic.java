package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.logging.MythicLogger;
import io.lumine.mythic.logging.MythicLogger.DebugLevel;
import io.lumine.mythic.mobs.ActiveMob;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.damage.DamagingMechanic;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class DamageAsMechanic extends DamagingMechanic implements ITargetedEntitySkill {
   private final PlaceholderDouble amount;
   private final String creditMode;
   private final double maxRadius;

   public DamageAsMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.amount = PlaceholderDouble.of(config.getString(new String[]{"amount", "a"}, "1", new String[0]));
      this.creditMode = config.getString(new String[]{"credit", "as"}, "owner", new String[0]);
      this.maxRadius = config.getDouble(new String[]{"radius", "r"}, 50.0);
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      if (!target.isDead() && !data.getCaster().isUsingDamageSkill() && (!target.isLiving() || !(target.getHealth() <= 0.0))) {
         double dmg = this.amount.get(data, target) * data.getPower();
         Entity credited = this.resolveCreditedEntity(data, this.creditMode);
         boolean useProxyDamager = false;
         if (credited != null && !credited.isDead()) {
            Entity casterBukkit = BukkitAdapter.adapt(data.getCaster().getEntity());
            if (casterBukkit != null && credited.getWorld() == casterBukkit.getWorld()) {
               double distSq = credited.getLocation().distanceSquared(casterBukkit.getLocation());
               useProxyDamager = distSq <= this.maxRadius * this.maxRadius;
            }
         }

         if (useProxyDamager) {
            Entity damager = credited;
            Entity bTarget = BukkitAdapter.adapt(target);
            if (damager != null && bTarget instanceof LivingEntity) {
               ((LivingEntity)bTarget).damage(dmg, damager);
               MythicLogger.debug(
                  DebugLevel.MECHANIC, "+ DamageAsMechanic (credit={0}) fired for {1} dmg by proxied damager", new Object[]{this.creditMode, dmg}
               );
               return SkillResult.SUCCESS;
            }

            MythicLogger.debug(DebugLevel.MECHANIC, "+ DamageAsMechanic credit failed, fallback to caster doDamage", new Object[0]);
         }

         this.doDamage(data.getCaster(), target, dmg);
         MythicLogger.debug(DebugLevel.MECHANIC, "+ DamageAsMechanic fallback fired for {0} with {1} power", new Object[]{dmg, data.getPower()});
         return SkillResult.SUCCESS;
      } else {
         return SkillResult.FAILURE;
      }
   }

   private Entity resolveCreditedEntity(SkillMetadata data, String mode) {
      if (!(data.getCaster() instanceof ActiveMob)) {
         return null;
      }

      ActiveMob am = (ActiveMob)data.getCaster();
      if ("parent".equalsIgnoreCase(mode)) {
         SkillCaster parent = am.getParent();
         if (parent != null) {
            AbstractEntity ae = parent.getEntity();
            return ae != null ? BukkitAdapter.adapt(ae) : null;
         } else {
            return null;
         }
      } else {
         try {
            Optional<UUID> opt = am.getOwner();
            if (opt != null && opt.isPresent()) {
               UUID uuid = (UUID)opt.get();
               Entity e = Bukkit.getEntity(uuid);
               if (e == null) {
                  e = Bukkit.getPlayer(uuid);
               }

               return e;
            }
         } catch (Throwable var7) {
         }

         return null;
      }
   }
}
