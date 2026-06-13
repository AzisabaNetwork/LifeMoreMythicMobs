package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractVector;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RhombusMechanic extends SkillMechanic implements ITargetedLocationSkill {
   private final PlaceholderDouble diagonalX;
   private final PlaceholderDouble diagonalZ;
   private final PlaceholderInt points;
   private final PlaceholderDouble yaw;
   private final PlaceholderDouble pitch;
   private final PlaceholderDouble roll;
   private final PlaceholderDouble xOffset;
   private final PlaceholderDouble yOffset;
   private final PlaceholderDouble zOffset;
   private final PlaceholderString onPointSkillName;
   private final PlaceholderDouble curvature;

   public RhombusMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.diagonalX = PlaceholderDouble.of(config.getString(new String[]{"diagonalx", "dx"}, "2", new String[0]));
      this.diagonalZ = PlaceholderDouble.of(config.getString(new String[]{"diagonalz", "dz"}, "2", new String[0]));
      this.points = PlaceholderInt.of(config.getString(new String[]{"points", "p"}, "32", new String[0]));
      this.yaw = PlaceholderDouble.of(config.getString(new String[]{"yaw", "y"}, "0", new String[0]));
      this.pitch = PlaceholderDouble.of(config.getString(new String[]{"pitch", "pi"}, "0", new String[0]));
      this.roll = PlaceholderDouble.of(config.getString(new String[]{"roll", "r"}, "0", new String[0]));
      this.xOffset = PlaceholderDouble.of(config.getString(new String[]{"xoffset", "ox", "x"}, "0", new String[0]));
      this.yOffset = PlaceholderDouble.of(config.getString(new String[]{"yoffset", "oy", "y"}, "0", new String[0]));
      this.zOffset = PlaceholderDouble.of(config.getString(new String[]{"zoffset", "oz", "z"}, "0", new String[0]));
      this.onPointSkillName = PlaceholderString.of(config.getString(new String[]{"onpointskillname", "onpointskill", "onpoint", "op"}, "", new String[0]));
      this.curvature = PlaceholderDouble.of(config.getString(new String[]{"curvature", "curve", "bend"}, "0", new String[0]));
   }

   public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
      double dx = this.diagonalX.get(data) / 2.0;
      double dz = this.diagonalZ.get(data) / 2.0;
      int pointCount = this.points.get(data);
      double yawDeg = this.yaw.get(data);
      double pitchDeg = this.pitch.get(data);
      double rollDeg = this.roll.get(data);
      double curvatureAmount = this.curvature.get(data);
      double xOffsetValue = this.xOffset.get(data);
      double yOffsetValue = this.yOffset.get(data);
      double zOffsetValue = this.zOffset.get(data);
      AbstractVector offsetVector = new AbstractVector(xOffsetValue, yOffsetValue, zOffsetValue);
      List<AbstractVector> rhombusOutLine = new ArrayList<>();
      AbstractVector[] vertices = new AbstractVector[]{
         new AbstractVector(0.0, 0.0, -dz), new AbstractVector(dx, 0.0, 0.0), new AbstractVector(0.0, 0.0, dz), new AbstractVector(-dx, 0.0, 0.0)
      };

      for (int i = 0; i < 4; i++) {
         AbstractVector from = vertices[i];
         AbstractVector to = vertices[(i + 1) % 4];
         AbstractVector edge = to.clone().subtract(from);
         AbstractVector normal = new AbstractVector(-edge.getZ(), 0.0, edge.getX()).normalize();

         for (int j = 0; j < pointCount / 4; j++) {
            double t = (double)j / (pointCount / 4);
            AbstractVector point = from.clone().multiply(1.0 - t).add(to.clone().multiply(t));
            double offsetFactor = Math.sin(Math.PI * t);
            AbstractVector curved = point.add(normal.clone().multiply(curvatureAmount * offsetFactor));
            curved = this.rotate(point, pitchDeg, yawDeg, rollDeg);
            curved = curved.add(offsetVector);
            rhombusOutLine.add(curved);
         }
      }

      Optional<Skill> onPointSkill = MythicBukkit.inst().getSkillManager().getSkill(this.onPointSkillName.get(data));

      for (AbstractVector vec : rhombusOutLine) {
         AbstractLocation loc = target.clone().add(vec);
         if (onPointSkill.isPresent()) {
            onPointSkill.get().execute(data.deepClone().setLocationTarget(loc).setOrigin(loc));
         }
      }

      return SkillResult.SUCCESS;
   }

   private AbstractVector rotate(AbstractVector v, double pitch, double yaw, double roll) {
      v = this.rotateAroundZ(v, roll);
      v = this.rotateAroundX(v, pitch);
      return this.rotateAroundY(v, -yaw);
   }

   private AbstractVector rotateAroundX(AbstractVector v, double angle) {
      double rad = Math.toRadians(angle);
      double cos = Math.cos(rad);
      double sin = Math.sin(rad);
      double y = v.getY() * cos - v.getZ() * sin;
      double z = v.getY() * sin + v.getZ() * cos;
      return new AbstractVector(v.getX(), y, z);
   }

   private AbstractVector rotateAroundY(AbstractVector v, double angle) {
      double rad = Math.toRadians(angle);
      double cos = Math.cos(rad);
      double sin = Math.sin(rad);
      double x = v.getX() * cos + v.getZ() * sin;
      double z = -v.getX() * sin + v.getZ() * cos;
      return new AbstractVector(x, v.getY(), z);
   }

   private AbstractVector rotateAroundZ(AbstractVector v, double angle) {
      double rad = Math.toRadians(angle);
      double cos = Math.cos(rad);
      double sin = Math.sin(rad);
      double x = v.getX() * cos - v.getY() * sin;
      double y = v.getX() * sin + v.getY() * cos;
      return new AbstractVector(x, y, v.getZ());
   }
}
