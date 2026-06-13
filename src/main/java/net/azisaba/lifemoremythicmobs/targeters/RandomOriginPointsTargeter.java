package net.azisaba.lifemoremythicmobs.targeters;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.targeters.ILocationSelector;
import io.lumine.mythic.util.annotations.MythicTargeter;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

@MythicTargeter(
   author = "igachi77",
   name = "randomOriginPoints",
   aliases = {"rop", "randomorigin"},
   description = "Targets random points inside a sphere around the origin"
)
public class RandomOriginPointsTargeter extends ILocationSelector {
   private final double radius;
   private final int amount;
   private final double xoffset;
   private final double yoffset;
   private final double zoffset;

   public RandomOriginPointsTargeter(MythicLineConfig config) {
      super(config);
      this.radius = Math.max(0.0, config.getDouble(new String[]{"radius", "r"}, 0.0));
      int a = config.getInteger(new String[]{"amount", "a"}, 1);
      this.amount = Math.max(1, a);
      this.xoffset = config.getDouble(new String[]{"xoffset", "xo", "x"}, 0.0);
      this.yoffset = config.getDouble(new String[]{"yoffset", "yo", "y"}, 0.0);
      this.zoffset = config.getDouble(new String[]{"zoffset", "zo", "z"}, 0.0);
   }

   public HashSet<AbstractLocation> getLocations(SkillMetadata data) {
      HashSet<AbstractLocation> targets = new HashSet<>();
      AbstractLocation center = data.getOrigin().clone().add(this.xoffset, this.yoffset, this.zoffset);
      if (this.radius <= 0.0) {
         targets.add(center);
         return targets;
      }

      int guard = 0;
      int guardMax = Math.max(1000, this.amount * 50);

      while (targets.size() < this.amount && guard++ < guardMax) {
         targets.add(this.randomPointInSphere(center, this.radius));
      }

      if (targets.isEmpty()) {
         targets.add(center);
      }

      return targets;
   }

   private AbstractLocation randomPointInSphere(AbstractLocation center, double radius) {
      ThreadLocalRandom rnd = ThreadLocalRandom.current();
      double u = rnd.nextDouble();
      double v = rnd.nextDouble();
      double w = rnd.nextDouble();
      double phi = (Math.PI * 2) * u;
      double cosTheta = 2.0 * v - 1.0;
      double sinTheta = Math.sqrt(Math.max(0.0, 1.0 - cosTheta * cosTheta));
      double r = radius * Math.cbrt(w);
      double x = r * sinTheta * Math.cos(phi);
      double y = r * cosTheta;
      double z = r * sinTheta * Math.sin(phi);
      return center.clone().add(x, y, z);
   }
}
