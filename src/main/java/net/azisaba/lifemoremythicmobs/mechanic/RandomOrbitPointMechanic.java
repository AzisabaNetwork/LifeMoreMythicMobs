package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

public class RandomOrbitPointMechanic extends SkillMechanic implements INoTargetSkill {
   private final int duration;
   private final int interval;
   private final int points;
   private final double minRadius;
   private final double maxRadius;
   private final double baseSpeed;
   private final double wander;
   private final double radialWander;
   private final Double yClampMin;
   private final Double yClampMax;
   private final boolean log;
   private final String onPointSkillName;
   private Optional<Skill> onPointSkill = Optional.empty();

   public RandomOrbitPointMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.duration = config.getInteger(new String[]{"duration", "d"}, 200);
      this.interval = Math.max(1, config.getInteger(new String[]{"interval", "i"}, 1));
      this.points = Math.max(1, config.getInteger(new String[]{"points", "p"}, 1));
      this.minRadius = Math.max(0.0, config.getDouble(new String[]{"minradius", "minr"}, 1.5));
      this.maxRadius = Math.max(this.minRadius, config.getDouble(new String[]{"maxradius", "maxr"}, 6.0));
      this.baseSpeed = config.getDouble(new String[]{"speed", "s"}, 0.18);
      this.wander = Math.max(0.0, config.getDouble(new String[]{"wander", "w"}, 0.02));
      this.radialWander = Math.max(0.0, config.getDouble(new String[]{"radialwander", "rw"}, 0.03));
      this.onPointSkillName = config.getString(new String[]{"onPoint", "onPointSkill", "oP", "op", "ops"}, null, new String[0]);
      Double yMin = config.getDouble(new String[]{"yclampmin", "ycmin"}, Double.NaN);
      Double yMax = config.getDouble(new String[]{"yclampmax", "ycmax"}, Double.NaN);
      this.yClampMin = Double.isNaN(yMin) ? null : yMin;
      this.yClampMax = Double.isNaN(yMax) ? null : yMax;
      this.log = config.getBoolean("log", false);
      MythicBukkit.inst().getSkillManager().queueSecondPass(() -> {
         if (this.onPointSkillName != null) {
            this.onPointSkill = MythicBukkit.inst().getSkillManager().getSkill(this.onPointSkillName);
            if (this.log) {
               IgaDebugLogger.log(this.getClass(), "onPoint resolved=" + this.onPointSkill.isPresent() + " name=" + this.onPointSkillName);
            }
         }
      });
   }

   public boolean cast(final SkillMetadata data) {
      final Entity bukkitCaster = BukkitAdapter.adapt(data.getCaster().getEntity());
      if (bukkitCaster == null || !bukkitCaster.isValid()) {
         return SkillResult.FAILURE;
      }

      if (!this.onPointSkill.isPresent()) {
         if (this.log) {
            IgaDebugLogger.log(this.getClass(), "onPoint skill is not resolved or missing. name=" + this.onPointSkillName);
         }

         return SkillResult.FAILURE;
      } else {
         final World world = bukkitCaster.getWorld();
         if (world == null) {
            return SkillResult.FAILURE;
         }

         final List<RandomOrbitPointMechanic.PointState> stars = new ArrayList<>(this.points);

         for (int i = 0; i < this.points; i++) {
            stars.add(RandomOrbitPointMechanic.PointState.randomInit(this.minRadius, this.maxRadius, this.baseSpeed));
         }

         final long repeats = Math.max(1L, (long)Math.ceil((double)Math.max(1, this.duration) / this.interval));
         final Skill onPoint = this.onPointSkill.get();
         (new BukkitRunnable() {
               long n = 0L;

               public void run() {
                  if (bukkitCaster.isValid() && !bukkitCaster.isDead()) {
                     if (this.n >= repeats) {
                        this.cancel();
                     } else {
                        Location center = bukkitCaster.getLocation();

                        for (RandomOrbitPointMechanic.PointState s : stars) {
                           s.tick(
                              RandomOrbitPointMechanic.this.minRadius,
                              RandomOrbitPointMechanic.this.maxRadius,
                              RandomOrbitPointMechanic.this.baseSpeed,
                              RandomOrbitPointMechanic.this.wander,
                              RandomOrbitPointMechanic.this.radialWander
                           );
                           double cosPhi = Math.cos(s.phi);
                           double dx = s.r * Math.cos(s.theta) * cosPhi;
                           double dy = s.r * Math.sin(s.phi);
                           double dz = s.r * Math.sin(s.theta) * cosPhi;
                           double x = center.getX() + dx;
                           double y = center.getY() + dy;
                           double z = center.getZ() + dz;
                           if (RandomOrbitPointMechanic.this.yClampMin != null && y < center.getY() + RandomOrbitPointMechanic.this.yClampMin) {
                              y = center.getY() * RandomOrbitPointMechanic.this.yClampMin;
                           }

                           if (RandomOrbitPointMechanic.this.yClampMax != null && y > center.getY() + RandomOrbitPointMechanic.this.yClampMax) {
                              y = center.getY() + RandomOrbitPointMechanic.this.yClampMax;
                           }

                           Location p = new Location(world, x, y, z);
                           SkillMetadata meta = data.deepClone();
                           AbstractLocation aloc = BukkitAdapter.adapt(p);
                           meta.setOrigin(aloc);
                           onPoint.execute(meta);
                        }

                        this.n++;
                     }
                  } else {
                     this.cancel();
                  }
               }
            })
            .runTaskTimer(MythicBukkit.inst(), 0L, this.interval);
         if (this.log) {
            IgaDebugLogger.log(
               this.getClass(),
               "started: duration="
                  + this.duration
                  + " interval="
                  + this.interval
                  + " points="
                  + this.points
                  + " r=["
                  + this.minRadius
                  + ","
                  + this.maxRadius
                  + "] speed="
                  + this.baseSpeed
                  + " wander="
                  + this.wander
                  + " radial="
                  + this.radialWander
            );
         }

         return SkillResult.SUCCESS;
      }
   }

   private static class PointState {
      double r;
      double theta;
      double phi;
      double wTheta;
      double wPhi;

      static RandomOrbitPointMechanic.PointState randomInit(double minR, double maxR, double baseSpeed) {
         ThreadLocalRandom rnd = ThreadLocalRandom.current();
         RandomOrbitPointMechanic.PointState s = new RandomOrbitPointMechanic.PointState();
         s.r = rnd.nextDouble(minR, Math.max(minR + 1.0E-6, maxR));
         s.theta = rnd.nextDouble(0.0, Math.PI * 2);
         s.phi = rnd.nextDouble(-Math.PI / 6, Math.PI / 6);
         double dir = rnd.nextBoolean() ? 1.0 : -1.0;
         s.wTheta = dir * baseSpeed * rnd.nextDouble(0.7, 1.3);
         s.wPhi = baseSpeed * 0.25 * rnd.nextDouble(-0.6, 0.6);
         return s;
      }

      void tick(double minR, double maxR, double baseSpeed, double wander, double radialWander) {
         ThreadLocalRandom rnd = ThreadLocalRandom.current();
         this.wTheta = this.wTheta + rnd.nextDouble(-wander, wander);
         this.wPhi = this.wPhi + rnd.nextDouble(-wander * 0.6, wander * 0.6);
         double maxWT = baseSpeed * 4.0;
         if (this.wTheta > maxWT) {
            this.wTheta = maxWT;
         }

         if (this.wTheta < -maxWT) {
            this.wTheta = -maxWT;
         }

         double maxWP = baseSpeed * 2.0;
         if (this.wPhi > maxWP) {
            this.wPhi = maxWP;
         }

         if (this.wPhi < -maxWP) {
            this.wPhi = -maxWP;
         }

         this.theta = this.theta + this.wTheta;
         this.phi = this.phi + this.wPhi;
         if (this.theta >= Math.PI * 2) {
            this.theta -= Math.PI * 2;
         }

         if (this.theta < 0.0) {
            this.theta += Math.PI * 2;
         }

         double phiLimit = 1.335176877775662;
         if (this.phi > phiLimit) {
            this.phi = phiLimit;
         }

         if (this.phi < -phiLimit) {
            this.phi = -phiLimit;
         }

         this.r = this.r + rnd.nextDouble(-radialWander, radialWander);
         if (this.r < minR) {
            this.r = minR + (minR - this.r) * 0.3;
         }

         if (this.r > maxR) {
            this.r = maxR - (this.r - maxR) * 0.3;
         }
      }
   }
}
