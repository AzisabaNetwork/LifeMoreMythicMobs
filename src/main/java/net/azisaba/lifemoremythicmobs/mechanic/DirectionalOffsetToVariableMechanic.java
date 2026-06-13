package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.util.VariableUtil;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;

public class DirectionalOffsetToVariableMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final PlaceholderDouble forwardOffset;
   private final PlaceholderDouble rightOffset;
   private final PlaceholderDouble yOffset;
   private final PlaceholderDouble yawOverride;
   private final PlaceholderString xVariable;
   private final PlaceholderString yVariable;
   private final PlaceholderString zVariable;

   public DirectionalOffsetToVariableMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.forwardOffset = PlaceholderDouble.of(config.getString(new String[]{"forward", "f"}, "0.0", new String[0]));
      this.rightOffset = PlaceholderDouble.of(config.getString(new String[]{"right", "r"}, "0.0", new String[0]));
      this.yOffset = PlaceholderDouble.of(config.getString(new String[]{"yoffset", "y"}, "0.0", new String[0]));
      this.yawOverride = PlaceholderDouble.of(config.getString(new String[]{"yaw"}, "null", new String[0]));
      this.xVariable = PlaceholderString.of(config.getString(new String[]{"xvar", "vx"}, "", new String[0]));
      this.yVariable = PlaceholderString.of(config.getString(new String[]{"yvar", "vy"}, "", new String[0]));
      this.zVariable = PlaceholderString.of(config.getString(new String[]{"zvar", "vz"}, "", new String[0]));
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      AbstractEntity caster = data.getCaster().getEntity();
      AbstractLocation baseLoc = caster.getLocation();
      double yawDeg = this.yawOverride != null ? this.yawOverride.get(data) : baseLoc.getYaw();
      double yawRad = Math.toRadians(-yawDeg);
      double forwardX = Math.sin(yawRad);
      double forwardZ = Math.cos(yawRad);
      double rightX = Math.sin(yawRad + (Math.PI / 2));
      double rightZ = Math.cos(yawRad + (Math.PI / 2));
      double f = this.forwardOffset.get(data);
      double r = this.rightOffset.get(data);
      double y = this.yOffset.get(data);
      double dx = forwardX * f + rightX * r;
      double dz = forwardZ * f + rightZ * r;
      double dy = y;
      AbstractLocation offsetLoc = baseLoc.clone().add(dx, dy, dz);
      String xVar = this.xVariable.get(data);
      String yVar = this.yVariable.get(data);
      String zVar = this.zVariable.get(data);
      if (!xVar.isEmpty()) {
         VariableUtil.setScopedVariable(xVar, String.valueOf(offsetLoc.getX()), data, target);
      }

      if (!yVar.isEmpty()) {
         VariableUtil.setScopedVariable(yVar, String.valueOf(offsetLoc.getY()), data, target);
      }

      if (!zVar.isEmpty()) {
         VariableUtil.setScopedVariable(zVar, String.valueOf(offsetLoc.getZ()), data, target);
      }

      return SkillResult.SUCCESS;
   }
}
