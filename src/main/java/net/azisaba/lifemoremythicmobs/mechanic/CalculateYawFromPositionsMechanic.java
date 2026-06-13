package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.util.VariableUtil;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;

public class CalculateYawFromPositionsMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final PlaceholderDouble fromX;
   private final PlaceholderDouble fromZ;
   private final PlaceholderDouble toX;
   private final PlaceholderDouble toZ;
   private final PlaceholderString yawVariable;

   public CalculateYawFromPositionsMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.fromX = PlaceholderDouble.of(config.getString("fromx", "0"));
      this.fromZ = PlaceholderDouble.of(config.getString("fromz", "0"));
      this.toX = PlaceholderDouble.of(config.getString("tox", "0"));
      this.toZ = PlaceholderDouble.of(config.getString("toz", "0"));
      this.yawVariable = PlaceholderString.of(config.getString("yawvar", "yaw"));
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      double fx = this.fromX.get(data);
      double fz = this.fromZ.get(data);
      double tx = this.toX.get(data);
      double tz = this.toZ.get(data);
      double dx = tx - fx;
      double dz = tz - fz;
      double angleRad = Math.atan2(-dx, dz);
      double angleDeg = Math.toDegrees(angleRad);
      if (angleDeg < 0.0) {
         angleDeg += 360.0;
      }

      VariableUtil.setScopedVariable(this.yawVariable.get(data), String.valueOf(angleDeg), data, target);
      return SkillResult.SUCCESS;
   }
}
