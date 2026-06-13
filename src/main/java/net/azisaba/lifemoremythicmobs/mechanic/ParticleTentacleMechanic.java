package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractVector;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
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
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleTentacleMechanic extends SkillMechanic implements ITargetedEntitySkill, ITargetedLocationSkill {
   private final PlaceholderDouble tentacleLength;
   private final PlaceholderDouble tentacleBend;
   private final PlaceholderDouble tentacleYaw;
   private final PlaceholderDouble tentacleCurveYaw;
   private final PlaceholderDouble tentacleChordAngle;
   private final PlaceholderInt tentaclePoints;
   private final PlaceholderString onPointSkill;
   private final boolean sequential;
   private final int interval;
   private final boolean fromOrigin;

   public ParticleTentacleMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.tentacleLength = config.getPlaceholderDouble(new String[]{"tentaclelength", "tl"}, 5.0, new String[0]);
      this.tentacleBend = config.getPlaceholderDouble(new String[]{"tentaclebend", "tb"}, 30.0, new String[0]);
      this.tentacleYaw = config.getPlaceholderDouble(new String[]{"tentacleyaw", "tyaw"}, 0.0, new String[0]);
      this.tentacleCurveYaw = config.getPlaceholderDouble(new String[]{"tentaclecurveyaw", "tcyaw"}, 0.0, new String[0]);
      this.tentacleChordAngle = config.getPlaceholderDouble(new String[]{"tentaclechordangle", "tca"}, 0.0, new String[0]);
      this.tentaclePoints = config.getPlaceholderInteger(new String[]{"tentaclepoints", "tp"}, 20, new String[0]);
      this.onPointSkill = PlaceholderString.of(config.getString(new String[]{"onpointskill", "onpoint", "ops"}, "", new String[0]));
      this.sequential = config.getBoolean("sequential", false);
      this.interval = config.getInteger(new String[]{"interval", "i"}, 1);
      this.fromOrigin = config.getBoolean(new String[]{"fromorigin", "fo"}, false);
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      return this.castAtLocation(data, target.getLocation());
   }

   public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
      AbstractLocation origin = this.fromOrigin ? data.getOrigin() : target;
      double length = Math.abs(this.tentacleLength.get(data));
      double bendDeg = this.tentacleBend.get(data);
      double yawDeg = this.tentacleYaw.get(data);
      double curveYawDeg = this.tentacleCurveYaw.get(data);
      double chordAngleDeg = this.tentacleChordAngle.get(data);
      int steps = this.tentaclePoints.get(data);
      final List<AbstractLocation> points = new ArrayList<>();
      double chordAngleRad = Math.toRadians(chordAngleDeg);
      AbstractVector forward = new AbstractVector(0, 0, 1);
      if (chordAngleDeg < 0.0) {
         forward = forward.multiply(-1);
      }

      forward = this.rotateAroundY(forward, yawDeg).normalize();
      AbstractVector up = new AbstractVector(0, 1, 0);
      AbstractVector right = this.crossProduct(forward, up).normalize();
      forward = this.rotateAroundAxis(forward, right, Math.toRadians(chordAngleRad)).normalize();
      if (curveYawDeg < 0.0) {
         right = right.multiply(-1);
      }

      AbstractVector curveAxis = this.rotateAroundVector(right, forward, Math.toRadians(Math.abs(curveYawDeg))).normalize();
      if (bendDeg < 0.0) {
         curveAxis = curveAxis.multiply(-1);
      }

      for (int i = 0; i <= steps; i++) {
         double t = (double)i / steps;
         double y = Math.cos(Math.abs(chordAngleRad)) * length * t;
         double zBase = Math.sin(Math.abs(chordAngleRad)) * length * t;
         double bendRadians = Math.toRadians(Math.sin(t * Math.PI) * bendDeg);
         double r = length / 3.0;
         AbstractVector offset = forward.clone()
            .multiply(zBase)
            .add(new AbstractVector(0.0, y, 0.0))
            .add(curveAxis.clone().multiply((Math.sin(bendRadians) + (1.0 - Math.cos(bendRadians)) * Math.signum(bendDeg)) * r));
         AbstractLocation point = origin.clone().add(offset);
         points.add(point);
      }

      Optional<Skill> onPoint = this.getSkill(this.onPointSkill.get(data));
      if (onPoint.isPresent()) {
         if (this.sequential) {
            (new BukkitRunnable(onPoint, data) {
               int index = 0;
               final Skill clonedSkill;
               final SkillMetadata base;

               {
                  this.clonedSkill = (Skill)var2.get();
                  this.base = var3.deepClone();
               }

               public void run() {
                  if (this.index >= points.size()) {
                     this.cancel();
                  } else {
                     AbstractLocation loc = points.get(this.index);
                     this.clonedSkill.execute(this.base.deepClone().setLocationTarget(loc).setOrigin(loc));
                     this.index++;
                  }
               }
            }).runTaskTimer(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), 0L, this.interval);
         } else {
            for (AbstractLocation loc : points) {
               onPoint.get().execute(data.deepClone().setLocationTarget(loc).setOrigin(loc));
            }
         }
      }

      return SkillResult.SUCCESS;
   }

   private AbstractVector rotateAroundY(AbstractVector vec, double degrees) {
      double radians = Math.toRadians(degrees);
      double cos = Math.cos(radians);
      double sin = Math.sin(radians);
      double x = vec.getX() * cos - vec.getZ() * sin;
      double z = vec.getX() * sin + vec.getZ() * cos;
      return new AbstractVector(x, vec.getY(), z);
   }

   private AbstractVector rotateAroundVector(AbstractVector vec, AbstractVector axis, double angle) {
      double x = vec.getX();
      double y = vec.getY();
      double z = vec.getZ();
      double u = axis.getX();
      double v = axis.getY();
      double w = axis.getZ();
      double cosA = Math.cos(angle);
      double sinA = Math.sin(angle);
      double dot = u * x + v * y + w * z;
      double newX = u * dot * (1.0 - cosA) + x * cosA + (-w * y + v * z) * sinA;
      double newY = v * dot * (1.0 - cosA) + y * cosA + (w * x - u * z) * sinA;
      double newZ = w * dot * (1.0 - cosA) + z * cosA + (-v * x + u * y) * sinA;
      return new AbstractVector(newX, newY, newZ);
   }

   private AbstractVector rotateAroundAxis(AbstractVector vec, AbstractVector axis, double angle) {
      double x = vec.getX();
      double y = vec.getY();
      double z = vec.getZ();
      double u = axis.getX();
      double v = axis.getY();
      double w = axis.getZ();
      double cosA = Math.cos(angle);
      double sinA = Math.sin(angle);
      double dot = u * x + v * y + w * z;
      double newX = u * dot * (1.0 - cosA) + x * cosA + (-w * y + v * z) * sinA;
      double newY = v * dot * (1.0 - cosA) + y * cosA + (w * x - u * z) * sinA;
      double newZ = w * dot * (1.0 - cosA) + z * cosA + (-v * x + u * y) * sinA;
      return new AbstractVector(newX, newY, newZ);
   }

   private AbstractVector crossProduct(AbstractVector a, AbstractVector b) {
      double x = a.getY() * b.getZ() - a.getZ() * b.getY();
      double y = a.getZ() * b.getX() - a.getX() * b.getZ();
      double z = a.getX() * b.getY() - a.getY() * b.getX();
      return new AbstractVector(x, y, z);
   }

   private Optional<Skill> getSkill(String name) {
      return name != null && !name.isEmpty() ? MythicBukkit.inst().getSkillManager().getSkill(name) : Optional.empty();
   }
}
