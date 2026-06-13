package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.AbstractSkill.ThreadSafetyLevel;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class WallAwareTeleportMechanic extends SkillMechanic implements ITargetedEntitySkill, ITargetedLocationSkill {
   private final double step;
   private final double maxRange;

   public WallAwareTeleportMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY;
      this.step = config.getDouble("step", 0.5);
      this.maxRange = config.getDouble("maxRange", 30.0);
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      Location entityLocation = BukkitAdapter.adapt(target.getLocation());
      Location centeredLocation = entityLocation.clone();
      centeredLocation.setX(entityLocation.getBlockX() + 0.5);
      centeredLocation.setZ(entityLocation.getBlockZ() + 0.5);
      return this.castAtLocation(data, BukkitAdapter.adapt(centeredLocation));
   }

   public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
      AbstractEntity caster = data.getCaster().getEntity();
      Location start = BukkitAdapter.adapt(caster.getLocation());
      Location end = BukkitAdapter.adapt(target);
      double distanceToTarget = start.distance(end);
      if (distanceToTarget > this.maxRange) {
         return SkillResult.FAILURE;
      }

      Vector direction = end.toVector().subtract(start.toVector()).normalize();
      Location lastValidTeleportLocation = null;

      for (double d = 0.0; d <= distanceToTarget; d += this.step) {
         Location current = start.clone().add(direction.clone().multiply(d));
         Location feet = current.getBlock().getLocation();
         Location head = feet.clone().add(0.0, 1.0, 0.0);
         if (!feet.getBlock().isPassable()) {
            break;
         }

         if (feet.getBlock().isPassable() && head.getBlock().isPassable()) {
            lastValidTeleportLocation = current.clone();
         }
      }

      if (lastValidTeleportLocation != null) {
         caster.teleport(BukkitAdapter.adapt(lastValidTeleportLocation));
         return SkillResult.SUCCESS;
      } else {
         return SkillResult.FAILURE;
      }
   }
}
