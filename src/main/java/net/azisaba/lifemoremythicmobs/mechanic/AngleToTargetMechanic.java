package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.util.VariableUtil;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import org.bukkit.Location;

public class AngleToTargetMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final String yawVar;
   private final String pitchVar;

   public AngleToTargetMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.yawVar = config.getString("yaw", "skill.var.yaw");
      this.pitchVar = config.getString("pitch", "skill.var.pitch");
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      AbstractEntity caster = data.getCaster().getEntity();
      if (caster != null && target != null) {
         if (caster.getBukkitEntity().getWorld() != target.getBukkitEntity().getWorld()) {
            return SkillResult.FAILURE;
         }

         Location from = caster.getBukkitEntity().getLocation();
         Location to = target.getBukkitEntity().getLocation();
         double dx = to.getX() - from.getX();
         double dy = to.getY() + target.getBukkitEntity().getHeight() / 2.0 - (from.getY() + caster.getBukkitEntity().getHeight());
         double dz = to.getZ() - from.getZ();
         float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
         double distanceXZ = Math.sqrt(dx * dx + dz * dz);
         float pitch = (float)Math.toDegrees(-Math.atan2(dy, distanceXZ));
         if (yaw < -180.0F) {
            yaw += 360.0F;
         }

         if (yaw > 180.0F) {
            yaw -= 360.0F;
         }

         VariableUtil.setScopedVariable(this.yawVar, String.format("%.2f", yaw), data, target);
         VariableUtil.setScopedVariable(this.pitchVar, String.format("%.2f", pitch), data, target);
         return SkillResult.SUCCESS;
      } else {
         return SkillResult.FAILURE;
      }
   }
}
