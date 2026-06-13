package net.azisaba.lifemoremythicmobs.targeters;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.targeters.ILocationSelector;
import java.util.HashSet;

public class DirectionalOffsetTargeter extends ILocationSelector {
   private final PlaceholderDouble forwardOffset;
   private final PlaceholderDouble rightOffset;
   private final PlaceholderDouble yOffset;
   private final PlaceholderDouble yawOverride;

   public DirectionalOffsetTargeter(MythicLineConfig config) {
      super(config);
      this.forwardOffset = PlaceholderDouble.of(config.getString(new String[]{"forward", "f"}, "0.0", new String[0]));
      this.rightOffset = PlaceholderDouble.of(config.getString(new String[]{"right", "r"}, "0.0", new String[0]));
      this.yOffset = PlaceholderDouble.of(config.getString(new String[]{"yoffset", "y"}, "0.0", new String[0]));
      this.yawOverride = PlaceholderDouble.of(config.getString(new String[]{"yaw"}, null, new String[0]));
   }

   public HashSet<AbstractLocation> getLocations(SkillMetadata data) {
      AbstractEntity caster = data.getCaster().getEntity();
      AbstractLocation baseLoc = caster.getLocation();
      double yawDeg;
      if (this.yawOverride != null) {
         yawDeg = this.yawOverride.get(data);
      } else {
         yawDeg = baseLoc.getYaw();
      }

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
      AbstractLocation targetLoc = baseLoc.clone().add(dx, dy, dz);
      HashSet<AbstractLocation> result = new HashSet<>();
      result.add(targetLoc);
      return result;
   }
}
