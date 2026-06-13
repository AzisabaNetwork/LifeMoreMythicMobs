package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.adapters.bukkit.BukkitEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class RrraytraceMechanic extends SkillMechanic implements INoTargetSkill {
   private final String entitySkillName;
   private final String locationSkillName;
   private final double maxRange;
   private final boolean pierceEntities;
   private final boolean fireLocationSkillOnMaxRange;
   private final String raytraceConditionString;
   private List<SkillCondition> raytraceConditions = null;

   public RrraytraceMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.entitySkillName = config.getString(new String[]{"onentityskill", "onentity", "oe", "es"}, "", new String[0]);
      this.locationSkillName = config.getString(new String[]{"onlocationskill", "onlocation", "ol", "ls"}, "", new String[0]);
      this.maxRange = config.getDouble(new String[]{"maxrange", "mr"}, 20.0);
      this.pierceEntities = config.getBoolean(new String[]{"pierceentities", "pe"}, false);
      this.raytraceConditionString = config.getString(new String[]{"raytraceconditions", "rconditions", "rcond"}, null, new String[0]);
      this.fireLocationSkillOnMaxRange = config.getBoolean(new String[]{"maxrangeskill", "mrs"}, true);
      if (this.raytraceConditionString != null) {
         this.raytraceConditions = getPlugin().getSkillManager().getConditions(this.raytraceConditionString);
      }
   }

   public SkillResult cast(SkillMetadata data) {
      AbstractEntity caster = data.getCaster().getEntity();
      if (!(caster instanceof BukkitEntity)) {
         return SkillResult.FAILURE;
      }

      Player player = (Player)((BukkitEntity)caster).getBukkitEntity();
      Location start = player.getEyeLocation();
      Vector direction = start.getDirection().normalize();
      Location blockHitLocation = this.rayTraceAndCast(player, start, direction, data);
      if (blockHitLocation != null && this.locationSkillName != null && !this.locationSkillName.isEmpty()) {
         AbstractLocation loc = BukkitAdapter.adapt(blockHitLocation);
         MythicBukkit.inst().getSkillManager().getSkill(this.locationSkillName).ifPresent(skill -> skill.execute(data.deepClone().setLocationTarget(loc)));
      }

      return SkillResult.SUCCESS;
   }

   private Location rayTraceAndCast(Player player, Location start, Vector direction, SkillMetadata data) {
      World world = player.getWorld();
      double step = 0.5;
      Location current = start.clone();
      boolean hitAnyEntity = false;

      label77:
      for (double traveled = 0.0; traveled <= this.maxRange; traveled += step) {
         current.add(direction.clone().multiply(step));
         if (current.getBlock().getType().isSolid()) {
            return hitAnyEntity ? null : current;
         }

         Iterator var13 = world.getNearbyEntities(current, 0.75, 0.75, 0.75).iterator();

         while (true) {
            AbstractEntity ae;
            while (true) {
               if (!var13.hasNext()) {
                  continue label77;
               }

               Entity entity = (Entity)var13.next();
               if (entity != player && entity instanceof LivingEntity) {
                  ae = BukkitAdapter.adapt(entity);
                  if (this.raytraceConditions == null) {
                     break;
                  }

                  boolean allPassed = true;

                  for (SkillCondition condition : this.raytraceConditions) {
                     boolean result = condition.evaluateEntity(ae);
                     if (!result) {
                        allPassed = false;
                        break;
                     }
                  }

                  if (allPassed) {
                     break;
                  }
               }
            }

            if (this.entitySkillName != null && !this.entitySkillName.isEmpty()) {
               SkillMetadata cloned = data.deepClone().setEntityTarget(ae);
               AbstractLocation hitLoc = BukkitAdapter.adapt(current.clone());
               cloned.setOrigin(hitLoc);
               MythicBukkit.inst().getSkillManager().getSkill(this.entitySkillName).ifPresent(skill -> skill.execute(cloned));
            }

            hitAnyEntity = true;
            if (!this.pierceEntities) {
               return null;
            }
         }
      }

      return !hitAnyEntity && this.fireLocationSkillOnMaxRange ? current : null;
   }
}
