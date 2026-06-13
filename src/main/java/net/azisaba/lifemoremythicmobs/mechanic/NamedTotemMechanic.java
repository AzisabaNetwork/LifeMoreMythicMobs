package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.logging.MythicLogger;
import io.lumine.mythic.api.skills.AbstractSkill;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.api.skills.projectiles.Projectile;
import io.lumine.mythic.api.skills.projectiles.Projectile.BulletType;
import io.lumine.mythic.api.skills.projectiles.Projectile.ProjectileTracker;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NamedTotemMechanic extends Projectile implements ITargetedEntitySkill, ITargetedLocationSkill {
   protected final int maxCharges;
   protected final float yOffset;
   protected final PlaceholderString totemNamePS;

   public NamedTotemMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.maxCharges = config.getInteger(new String[]{"charges", "ch", "c"}, 0);
      this.yOffset = config.getFloat(new String[]{"yoffset", "yo"}, 1.0F);
      this.stopOnHitEntity = config.getBoolean(new String[]{"stopatentity", "se"}, false);
      String rawName = config.getString(new String[]{"totemname", "name", "id"}, null, new String[0]);
      this.totemNamePS = rawName != null ? PlaceholderString.of(rawName) : null;
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      try {
         String resolvedName = this.resolveName(data);
         UUID owner = resolvedOwnerUuidFromCaster(data);
         new NamedTotemMechanic.TotemTracker(data, target.getLocation(), resolvedName, owner);
         return SkillResult.SUCCESS;
      } catch (Exception ex) {
         MythicLogger.error("An error occurred executing a NamedTotem Mechanic (entity)", ex);
         return SkillResult.FAILURE;
      }
   }

   public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
      try {
         String resolvedName = this.resolveName(data);
         UUID owner = resolvedOwnerUuidFromCaster(data);
         new NamedTotemMechanic.TotemTracker(data, target, resolvedName, owner);
         return SkillResult.SUCCESS;
      } catch (Exception ex) {
         MythicLogger.error("An error occurred executing a NamedTotem Mechanic (location", ex);
         return SkillResult.FAILURE;
      }
   }

   private String resolveName(SkillMetadata data) {
      if (this.totemNamePS == null) {
         return null;
      }

      AbstractEntity ref = data.getCaster() != null ? data.getCaster().getEntity() : null;
      String name = this.totemNamePS.get(data, ref);
      if (name != null) {
         name = name.trim();
         if (name.isEmpty()) {
            name = null;
         }
      }

      return name;
   }

   private static UUID resolvedOwnerUuidFromCaster(SkillMetadata data) {
      AbstractEntity ce = data != null && data.getCaster() != null ? data.getCaster().getEntity() : null;
      if (ce == null) {
         return null;
      }

      try {
         return ce.getUniqueId();
      } catch (Throwable t) {
         try {
            return ce.getBukkitEntity().getUniqueId();
         } catch (Throwable t2) {
            return null;
         }
      }
   }

   public static final class TotemRegistry {
      private static final ConcurrentMap<String, ConcurrentMap<UUID, Set<ProjectileTracker>>> BY_NAME_OWNER = new ConcurrentHashMap<>();

      private TotemRegistry() {
      }

      public static void register(String name, UUID owner, ProjectileTracker tracker) {
         BY_NAME_OWNER.computeIfAbsent(name, k -> new ConcurrentHashMap<>()).computeIfAbsent(owner, k -> ConcurrentHashMap.newKeySet()).add(tracker);
      }

      public static void unregister(String name, UUID owner, ProjectileTracker tracker) {
         ConcurrentMap<UUID, Set<ProjectileTracker>> byOwner = BY_NAME_OWNER.get(name);
         if (byOwner != null) {
            Set<ProjectileTracker> set = byOwner.get(owner);
            if (set != null) {
               set.remove(tracker);
               if (set.isEmpty()) {
                  byOwner.remove(name);
               }
            }

            if (byOwner.isEmpty()) {
               BY_NAME_OWNER.remove(name);
            }
         }
      }

      public static int terminate(String name, UUID owner) {
         ConcurrentMap<UUID, Set<ProjectileTracker>> byOwner = BY_NAME_OWNER.get(name);
         if (byOwner == null) {
            return 0;
         }

         Set<ProjectileTracker> set = byOwner.remove(owner);
         if (set != null && !set.isEmpty()) {
            int count = 0;

            for (ProjectileTracker t : set) {
               try {
                  t.setCancelled();
                  count++;
               } catch (Throwable var8) {
               }
            }

            if (byOwner.isEmpty()) {
               BY_NAME_OWNER.remove(name);
            }

            return count;
         } else {
            if (byOwner.isEmpty()) {
               BY_NAME_OWNER.remove(name);
            }

            return 0;
         }
      }

      public static int count(String name, UUID owner) {
         ConcurrentMap<UUID, Set<ProjectileTracker>> byOwner = BY_NAME_OWNER.get(name);
         if (byOwner == null) {
            return 0;
         }

         Set<ProjectileTracker> set = byOwner.get(owner);
         return set == null ? 0 : set.size();
      }
   }

   private class TotemTracker extends ProjectileTracker {
      private final AbstractLocation position;
      private int charges;
      private final String totemName;
      private final UUID ownerUuid;
      private boolean unregistered = false;

      public TotemTracker(SkillMetadata data, AbstractLocation target, String totemName, UUID ownerUuid) {
         super(NamedTotemMechanic.this, data, target);
         this.position = target;
         this.charges = NamedTotemMechanic.this.maxCharges;
         this.totemName = totemName;
         this.ownerUuid = ownerUuid;
         if (this.totemName != null && this.ownerUuid != null) {
            NamedTotemMechanic.TotemRegistry.register(this.totemName, this.ownerUuid, this);
         }

         this.start();
      }

      public void projectileStart() {
         this.startLocation = this.position;
         this.currentLocation = this.position;
         if (NamedTotemMechanic.this.yOffset != 0.8F) {
            this.currentLocation.setY(this.currentLocation.getY() + NamedTotemMechanic.this.yOffset);
         }
      }

      public void projectileTick() {
         if (this.bullet != null) {
            if (NamedTotemMechanic.this.bulletType == BulletType.ITEM) {
               AbstractLocation ol = this.currentLocation.clone();
               AbstractSkill.getPlugin().getVolatileCodeHandler().getEntityHandler().setItemPosition(this.bullet, ol);
               this.bullet.setVelocity(this.currentLocation.toVector().subtract(this.previousLocation.toVector()));
            } else if (NamedTotemMechanic.this.bulletSpin != 0.0F) {
               AbstractLocation ol = this.currentLocation.clone();
               if (NamedTotemMechanic.this.bulletSpin != 0.0F) {
                  float newSpin = this.bullet.getLocation().getYaw() + NamedTotemMechanic.this.bulletSpin;
                  ol.setYaw(newSpin);
               }

               this.bullet.teleport(ol);
            } else {
               this.bullet.setVelocity(this.currentLocation.toVector().subtract(this.bullet.getLocation().clone().toVector()).multiply(1));
               if (NamedTotemMechanic.this.bulletSpin > 0.0F) {
                  float newSpin = this.bullet.getLocation().getYaw() + NamedTotemMechanic.this.bulletSpin;
                  AbstractSkill.getPlugin().getVolatileCodeHandler().getEntityHandler().setEntityRotation(this.bullet, newSpin, newSpin);
               }
            }
         }

         if (this.inRange != null && this.inRange.size() > 0) {
            for (AbstractEntity e : this.inRange) {
               if (!e.isDead() && this.getBoundingBox().overlaps(e.getBukkitEntity().getBoundingBox())) {
                  this.targets.add(e);
                  this.immune.put(e, System.currentTimeMillis());
                  break;
               }
            }

            this.immune.entrySet().removeIf(entry -> (Long)entry.getValue() < System.currentTimeMillis() - 2000L);
         }

         if (NamedTotemMechanic.this.onTickSkill.isPresent() && ((Skill)NamedTotemMechanic.this.onTickSkill.get()).isUsable(this.data)) {
            SkillMetadata sData = this.data.deepClone();
            AbstractLocation location = NamedTotemMechanic.this.bulletType == BulletType.ARROW ? this.previousLocation.clone() : this.currentLocation.clone();
            HashSet<AbstractLocation> targets = new HashSet<>();
            targets.add(location);
            sData.setLocationTargets(targets);
            sData.setOrigin(location);
            ((Skill)NamedTotemMechanic.this.onTickSkill.get()).execute(sData);
         }

         if (this.targets.size() > 0) {
            this.doHit(new HashSet<>(this.targets));
            if (NamedTotemMechanic.this.stopOnHitEntity) {
               this.terminate();
            }

            this.charges--;
            if (NamedTotemMechanic.this.maxCharges > 0 && this.charges <= 0) {
               this.terminate();
            }
         }

         this.targets.clear();
      }

      public void doHit(HashSet<AbstractEntity> targets) {
         if (NamedTotemMechanic.this.onHitSkill.isPresent() && ((Skill)NamedTotemMechanic.this.onHitSkill.get()).isUsable(this.data)) {
            SkillMetadata sData = this.data.deepClone();
            sData.setEntityTargets(targets);
            sData.setOrigin(this.currentLocation.clone());
            ((Skill)NamedTotemMechanic.this.onHitSkill.get()).execute(sData);
         }
      }

      public void setCancelled() {
         this.tryUnregister();
         this.terminate();
      }

      public boolean getCancelled() {
         return this.components.hasTerminated();
      }

      public void projectileEnd() {
         this.tryUnregister();
         super.projectileEnd();
      }

      private void tryUnregister() {
         if (!this.unregistered && this.totemName != null && this.ownerUuid != null) {
            NamedTotemMechanic.TotemRegistry.unregister(this.totemName, this.ownerUuid, this);
            this.unregistered = true;
         }
      }

      public void applyBulletVelocity() {
      }
   }
}
