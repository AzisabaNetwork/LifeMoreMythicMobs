package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.util.VariableUtil;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;

public class TargetYawFromOriginMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final PlaceholderString variable;

   public TargetYawFromOriginMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.variable = PlaceholderString.of(config.getString(new String[]{"value", "val", "v"}, "", new String[0]));
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      if (target == null) {
         return SkillResult.FAILURE;
      }

      AbstractLocation origin = data.getOrigin();
      if (origin == null) {
         origin = data.getCaster().getEntity().getLocation();
      }

      AbstractLocation targetLoc = target.getLocation();
      if (targetLoc == null) {
         return SkillResult.FAILURE;
      }

      double dx = targetLoc.getX() - origin.getX();
      double dz = targetLoc.getZ() - origin.getZ();
      double yaw = Math.toDegrees(Math.atan2(-dx, dz));
      if (yaw < 0.0) {
         yaw += 360.0;
      }

      String scopedValueKey = this.variable.get(data);
      if (scopedValueKey.startsWith("<") && scopedValueKey.endsWith(">")) {
         scopedValueKey = scopedValueKey.substring(1, scopedValueKey.length() - 1);
      }

      if (!scopedValueKey.matches("^(caster|target|skill|global)\\.var\\..*$")) {
         return SkillResult.FAILURE;
      }

      VariableUtil.setScopedVariable(scopedValueKey, String.valueOf(yaw), data, target);
      return SkillResult.SUCCESS;
   }
}
