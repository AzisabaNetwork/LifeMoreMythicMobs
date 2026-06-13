package net.azisaba.lifemoremythicmobs.targeters;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.targeters.ILocationSelector;
import java.util.HashSet;
import org.bukkit.util.Vector;

public class RingAroundOriginTargeter extends ILocationSelector {
   private final PlaceholderDouble radius;
   private final PlaceholderInt points;
   private final boolean rotate;
   private final PlaceholderDouble xRotationDeg;
   private final PlaceholderDouble yRotationDeg;
   private final PlaceholderDouble zRotationDeg;
   private final PlaceholderDouble startDegAngle;

   public RingAroundOriginTargeter(MythicLineConfig config) {
      super(config);
      this.radius = config.getPlaceholderDouble(new String[]{"radius", "r"}, 3.0, new String[0]);
      this.points = config.getPlaceholderInteger(new String[]{"points", "p"}, 8, new String[0]);
      this.rotate = config.getBoolean("rotate", false);
      this.xRotationDeg = config.getPlaceholderDouble("xRotation", 0.0);
      this.yRotationDeg = config.getPlaceholderDouble("yRotation", 0.0);
      this.zRotationDeg = config.getPlaceholderDouble("zRotation", 0.0);
      this.startDegAngle = config.getPlaceholderDouble(new String[]{"startdegangle", "sa"}, 0.0, new String[0]);
   }

   public HashSet<AbstractLocation> getLocations(SkillMetadata data) {
      HashSet<AbstractLocation> locations = new HashSet<>();
      AbstractLocation origin = data.getOrigin();
      if (origin == null) {
         return locations;
      }

      double r = this.radius.get(data);
      int count = this.points.get(data);
      boolean doRotate = this.rotate;
      double xRad = Math.toRadians(this.xRotationDeg.get(data));
      double yRad = Math.toRadians(this.yRotationDeg.get(data));
      double zRad = Math.toRadians(this.zRotationDeg.get(data));
      double startRad = Math.toRadians(this.startDegAngle.get(data));
      if (count <= 0) {
         Vector offset = new Vector(this.xoffset, this.yoffset, this.zoffset);
         if (doRotate) {
            offset = this.applyRotation(offset, xRad, yRad, zRad);
         }

         AbstractLocation point = origin.clone().add(offset.getX(), offset.getY(), offset.getZ());
         locations.add(point);
         return locations;
      } else {
         for (int i = 0; i < count; i++) {
            double angle = startRad + (Math.PI * 2) * i / count;
            double x = r * Math.cos(angle);
            double z = r * Math.sin(angle);
            Vector pos = new Vector(x + this.xoffset, this.yoffset, z + this.zoffset);
            if (doRotate) {
               pos = this.applyRotation(pos, xRad, yRad, zRad);
            }

            AbstractLocation point = origin.clone().add(pos.getX(), pos.getY(), pos.getZ());
            locations.add(point);
         }

         return locations;
      }
   }

   private Vector applyRotation(Vector v, double xRad, double yRad, double zRad) {
      double x1 = v.getX() * Math.cos(zRad) - v.getY() * Math.sin(zRad);
      double y1 = v.getX() * Math.sin(zRad) + v.getY() * Math.cos(zRad);
      double z1 = v.getZ();
      v = new Vector(x1, y1, z1);
      double x2 = v.getX() * Math.cos(yRad) + v.getZ() * Math.sin(yRad);
      double y2 = v.getY();
      double z2 = -v.getX() * Math.sin(yRad) + v.getZ() * Math.cos(yRad);
      v = new Vector(x2, y2, z2);
      double x3 = v.getX();
      double y3 = v.getY() * Math.cos(xRad) - v.getZ() * Math.sin(xRad);
      double z3 = v.getY() * Math.sin(xRad) + v.getZ() * Math.cos(xRad);
      return new Vector(x3, y3, z3);
   }
}
