package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.mechanics.ParticleEffect;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class DrawQuadParticleMechanic extends ParticleEffect {
   private final Vector p1;
   private final Vector p2;
   private final Vector p3;
   private final Vector p4;
   private final int pointsPerEdge;

   public DrawQuadParticleMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.p1 = this.parseVector(config.getString(new String[]{"point1", "p1"}, "0,0,0", new String[0]));
      this.p2 = this.parseVector(config.getString(new String[]{"point2", "p2"}, "0,0,0", new String[0]));
      this.p3 = this.parseVector(config.getString(new String[]{"point3", "p3"}, "0,0,0", new String[0]));
      this.p4 = this.parseVector(config.getString(new String[]{"point4", "p4"}, "0,0,0", new String[0]));
      this.pointsPerEdge = config.getInteger(new String[]{"pointsperedge", "ppe"}, 10);
   }

   public void playEffect(SkillMetadata data, AbstractLocation location) {
      Location origin = BukkitAdapter.adapt(location);
      World world = origin.getWorld();
      List<Vector> rawPoints = Arrays.asList(this.p1, this.p2, this.p3, this.p4);
      List<Vector> ordered = this.sortPointsClockwise(rawPoints);

      for (int i = 0; i < 4; i++) {
         Location start = origin.clone().add(ordered.get(i));
         Location end = origin.clone().add(ordered.get((i + 1) % 4));
         this.drawLine(data, start, end);
      }
   }

   private void drawLine(SkillMetadata data, Location start, Location end) {
      Vector diff = end.toVector().subtract(start.toVector());

      for (int i = 0; i <= this.pointsPerEdge; i++) {
         Vector point = start.toVector().add(diff.clone().multiply((double)i / this.pointsPerEdge));
         AbstractLocation loc = BukkitAdapter.adapt(point.toLocation(start.getWorld()));
         Collection<AbstractEntity> viewers = this.audience.get(data, data.getCaster().getEntity());
         this.playParticleEffect(data, loc, viewers);
      }
   }

   private Vector parseVector(String input) {
      String[] split = input.split(",");
      if (split.length != 3) {
         return new Vector(0, 0, 0);
      }

      try {
         double x = Double.parseDouble(split[0]);
         double y = Double.parseDouble(split[1]);
         double z = Double.parseDouble(split[2]);
         return new Vector(x, y, z);
      } catch (NumberFormatException e) {
         return new Vector(0, 0, 0);
      }
   }

   private List<Vector> sortPointsClockwise(List<Vector> points) {
      Vector centroid = new Vector(0, 0, 0);

      for (Vector v : points) {
         centroid.add(new Vector(v.getX(), 0.0, v.getZ()));
      }

      centroid.multiply(1.0 / points.size());
      points.sort(Comparator.comparingDouble(vx -> {
         double dx = vx.getX() - centroid.getX();
         double dz = vx.getZ() - centroid.getZ();
         return Math.atan2(dz, dx);
      }));
      return points;
   }
}
