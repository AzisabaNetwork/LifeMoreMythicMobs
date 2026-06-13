package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class WallPhaseMechanic extends SkillMechanic implements ITargetedEntitySkill {
   public WallPhaseMechanic(SkillExecutor executor, MythicLineConfig mlc) { super(executor, mlc.getLine(), mlc);
   }

   public boolean castAtEntity(SkillMetadata data, AbstractEntity abstractEntity) {
      ((LifeMoreMythicMobs)JavaPlugin.getPlugin(LifeMoreMythicMobs.class)).getLogger().info("wallPhaseメカニックの開始");
      if (!(abstractEntity.getBukkitEntity() instanceof LivingEntity)) {
         return SkillResult.FAILURE;
      }

      LivingEntity caster = (LivingEntity)abstractEntity.getBukkitEntity();
      Location eyeLocation = caster.getEyeLocation();
      Vector direction = eyeLocation.getDirection().normalize();
      double maxDistance = 100.0;
      double step = 0.5;
      Location wallStart = null;

      for (double d = 0.0; d <= maxDistance; d += step) {
         Location checkLoc = eyeLocation.clone().add(direction.clone().multiply(d));
         Block block = checkLoc.getBlock();
         if (block != null && block.getType().isSolid()) {
            wallStart = checkLoc.clone();
            break;
         }
      }

      if (wallStart == null) {
         return SkillResult.FAILURE;
      }

      Location wallEnd = null;

      for (double d = 0.0; d <= maxDistance; d += step) {
         Location checkLoc = wallStart.clone().add(direction.clone().multiply(d));
         Block block = checkLoc.getBlock();
         if (block != null && !block.getType().isSolid()) {
            wallEnd = checkLoc.clone();
            break;
         }
      }

      if (wallEnd == null) {
         return SkillResult.FAILURE;
      }

      caster.teleport(wallEnd);
      return SkillResult.SUCCESS;
   }
}
