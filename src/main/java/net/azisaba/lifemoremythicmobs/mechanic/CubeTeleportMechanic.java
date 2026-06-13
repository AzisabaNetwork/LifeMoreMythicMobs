package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;

public class CubeTeleportMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final PlaceholderDouble x1Var;
   private final PlaceholderDouble y1Var;
   private final PlaceholderDouble z1Var;
   private final PlaceholderDouble x2Var;
   private final PlaceholderDouble y2Var;
   private final PlaceholderDouble z2Var;

   public CubeTeleportMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.x1Var = PlaceholderDouble.of(config.getString("x1", "0"));
      this.y1Var = PlaceholderDouble.of(config.getString("y1", "0"));
      this.z1Var = PlaceholderDouble.of(config.getString("z1", "0"));
      this.x2Var = PlaceholderDouble.of(config.getString("x2", "0"));
      this.y2Var = PlaceholderDouble.of(config.getString("y2", "0"));
      this.z2Var = PlaceholderDouble.of(config.getString("z2", "0"));
   }

   public boolean castAtEntity(SkillMetadata data, AbstractEntity entity) {
      SkillCaster caster = data.getCaster();
      if (entity == null) {
         return SkillResult.FAILURE;
      } else {
         double x1 = this.x1Var.get(data, caster.getEntity());
         double y1 = this.y1Var.get(data, caster.getEntity());
         double z1 = this.z1Var.get(data, caster.getEntity());
         double x2 = this.x2Var.get(data, caster.getEntity());
         double y2 = this.y2Var.get(data, caster.getEntity());
         double z2 = this.z2Var.get(data, caster.getEntity());
         AbstractLocation targetLoc = entity.getLocation();
         double tx = targetLoc.getX();
         double ty = targetLoc.getY();
         double tz = targetLoc.getZ();
         boolean inX = Math.min(x1, x2) <= tx && tx <= Math.max(x1, x2);
         boolean inY = Math.min(y1, y2) <= ty && ty <= Math.max(y1, y2);
         boolean inZ = Math.min(z1, z2) <= tz && tz <= Math.max(z1, z2);
         if (inX && inY && inZ) {
            caster.getEntity().teleport(targetLoc);
            return SkillResult.SUCCESS;
         } else {
            return SkillResult.FAILURE;
         }
      }
   }
}
