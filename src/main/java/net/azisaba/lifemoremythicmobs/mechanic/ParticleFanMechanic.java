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
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleFanMechanic extends SkillMechanic implements ITargetedEntitySkill, ITargetedLocationSkill {
   private final PlaceholderDouble angle;
   private final PlaceholderDouble radius;
   private final PlaceholderDouble points;
   private final PlaceholderDouble yawRotation;
   private final PlaceholderDouble pitchRotation;
   private final boolean applyPitch;
   private final boolean followPitchDirection;
   private final boolean fill;
   private final boolean exponential;
   private final boolean reversed;
   private final PlaceholderDouble biasPower;
   private final PlaceholderDouble xOffset;
   private final PlaceholderDouble yOffset;
   private final PlaceholderDouble zOffset;
   private final PlaceholderDouble forwardOffset;
   private final PlaceholderDouble rollOffset;
   private final PlaceholderDouble startDegAngle;
   private final PlaceholderString onPointSkill;
   private final boolean sequential;
   private final int interval;
   private final boolean reverseOrder;

   public ParticleFanMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.angle = PlaceholderDouble.of(config.getString(new String[]{"angle", "a"}, "90", new String[0]));
      this.radius = PlaceholderDouble.of(config.getString(new String[]{"radius", "r"}, "3", new String[0]));
      this.points = PlaceholderDouble.of(config.getString(new String[]{"points", "p"}, "20", new String[0]));
      this.yawRotation = PlaceholderDouble.of(config.getString(new String[]{"yawrotation", "yrot", "yaw"}, "0", new String[0]));
      this.pitchRotation = PlaceholderDouble.of(config.getString(new String[]{"pitchrotation", "prot"}, "0", new String[0]));
      this.applyPitch = config.getBoolean("applyPitch", false);
      this.followPitchDirection = config.getBoolean("followpitch", false);
      this.fill = config.getBoolean("fill", false);
      this.exponential = config.getBoolean("exponential", false);
      this.reversed = config.getBoolean("reversed", false);
      this.biasPower = PlaceholderDouble.of(config.getString(new String[]{"biaspower", "bias"}, "1.5", new String[0]));
      this.xOffset = PlaceholderDouble.of(config.getString(new String[]{"xoffset", "ox"}, "0", new String[0]));
      this.yOffset = PlaceholderDouble.of(config.getString(new String[]{"yoffset", "oy"}, "0", new String[0]));
      this.zOffset = PlaceholderDouble.of(config.getString(new String[]{"zoffset", "oz"}, "0", new String[0]));
      this.forwardOffset = PlaceholderDouble.of(config.getString(new String[]{"forwardoffset", "fo"}, "0", new String[0]));
      this.rollOffset = PlaceholderDouble.of(config.getString(new String[]{"roll"}, "0", new String[0]));
      this.startDegAngle = PlaceholderDouble.of(config.getString(new String[]{"startdegangle", "sa"}, "0", new String[0]));
      this.onPointSkill = PlaceholderString.of(config.getString(new String[]{"onpointskill", "ops", "onpoint", "op"}, "", new String[0]));
      this.sequential = config.getBoolean("sequential", false);
      this.interval = config.getInteger(new String[]{"interval", "i"}, 1);
      this.reverseOrder = config.getBoolean("reverseorder", false);
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      return this.castAtLocation(data, target.getLocation());
   }

   public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
      int pointCount = (int)this.points.get(data);
      double arcAngle = this.angle.get(data);
      double rad = this.radius.get(data);
      double rollOffsetDeg = this.rollOffset.get(data);
      double xOffsetVal = this.xOffset.get(data);
      double yOffsetVal = this.yOffset.get(data);
      double zOffsetVal = this.zOffset.get(data);
      double forwardOffsetVal = this.forwardOffset.get(data);
      double bias = this.biasPower.get(data);
      double startAngle = this.startDegAngle.get(data);
      double yawRotVal = this.yawRotation.get(data);
      AbstractLocation origin = target.clone();
      double baseYaw = origin.getYaw();
      double basePitch = origin.getPitch();
      double yaw = -baseYaw + startAngle + yawRotVal;
      double pitch = 0.0;
      if (this.followPitchDirection) {
         pitch = basePitch;
      } else if (this.applyPitch) {
         pitch = this.pitchRotation.get(data);
      }

      if (!this.followPitchDirection) {
         origin.add(0.0, yOffsetVal, 0.0);
      } else {
         origin.add(0.0, yOffsetVal, 0.0);
      }

      origin.add(xOffsetVal, 0.0, zOffsetVal);
      double pitchRad = Math.toRadians(this.followPitchDirection ? pitch : 0.0);
      double yawRad = Math.toRadians(baseYaw);
      double dx = -Math.sin(yawRad) * Math.cos(pitchRad);
      double dy = -Math.sin(pitchRad);
      double dz = Math.cos(yawRad) * Math.cos(pitchRad);
      origin.add(dx * forwardOffsetVal, dy * forwardOffsetVal, dz * forwardOffsetVal);
      AbstractVector rotation = new AbstractVector(!this.applyPitch && !this.followPitchDirection ? 0.0 : pitch, yaw, rollOffsetDeg);
      List<AbstractLocation> particlePoints = new ArrayList<>();
      int layers = !this.sequential && this.fill ? (int)rad : 1;

      for (int r = 1; r <= layers; r++) {
         double radiusLayer = this.fill ? rad * r / layers : rad;

         for (int i = 0; i < pointCount; i++) {
            double t = (double)i / (pointCount - 1);
            if (this.exponential) {
               t = Math.pow(t, bias);
               if (this.reversed) {
                  t = 1.0 - t;
               }
            }

            double theta = Math.toRadians(-arcAngle / 2.0 + arcAngle * t);
            double x = radiusLayer * Math.sin(theta);
            double z = radiusLayer * Math.cos(theta);
            AbstractVector localVec = new AbstractVector(x, 0.0, z);
            localVec = this.rotateAroundAxisZ(localVec, rotation.getZ());
            localVec = this.rotateAroundAxisX(localVec, rotation.getX());
            localVec = this.rotateAroundAxisY(localVec, rotation.getY());
            AbstractLocation point = origin.clone().add(localVec);
            particlePoints.add(point);
         }
      }

      Optional<Skill> onPoint = this.getSkill(this.onPointSkill.get(data));
      if (onPoint.isPresent()) {
         if (this.sequential) {
            final List<AbstractLocation> list = this.reverseOrder ? this.reverseList(particlePoints) : particlePoints;
            (new BukkitRunnable(onPoint, data) {
               int index = 0;
               final Skill clonedSkill;
               final SkillMetadata base;

               {
                  this.clonedSkill = (Skill)var2.get();
                  this.base = var3.deepClone();
               }

               public void run() {
                  if (this.index >= list.size()) {
                     this.cancel();
                  } else {
                     AbstractLocation loc = list.get(this.index);
                     this.clonedSkill.execute(this.base.deepClone().setLocationTarget(loc).setOrigin(loc));
                     this.index++;
                  }
               }
            }).runTaskTimer(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), 0L, this.interval);
         } else {
            for (AbstractLocation loc : particlePoints) {
               onPoint.get().execute(data.deepClone().setLocationTarget(loc).setOrigin(loc));
            }
         }
      }

      return SkillResult.SUCCESS;
   }

   private Optional<Skill> getSkill(String name) {
      return name != null && !name.isEmpty() ? MythicBukkit.inst().getSkillManager().getSkill(name) : Optional.empty();
   }

   private AbstractVector rotateAroundAxisX(AbstractVector v, double angle) {
      angle = Math.toRadians(angle);
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      double y = v.getY() * cos - v.getZ() * sin;
      double z = v.getY() * sin + v.getZ() * cos;
      return new AbstractVector(v.getX(), y, z);
   }

   private AbstractVector rotateAroundAxisY(AbstractVector v, double angle) {
      angle = Math.toRadians(angle);
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      double x = v.getX() * cos + v.getZ() * sin;
      double z = -v.getX() * sin + v.getZ() * cos;
      return new AbstractVector(x, v.getY(), z);
   }

   private AbstractVector rotateAroundAxisZ(AbstractVector v, double angle) {
      angle = Math.toRadians(angle);
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      double x = v.getX() * cos - v.getY() * sin;
      double y = v.getX() * sin + v.getY() * cos;
      return new AbstractVector(x, y, v.getZ());
   }

   private List<AbstractLocation> reverseList(List<AbstractLocation> list) {
      List<AbstractLocation> copy = new ArrayList<>(list);
      Collections.reverse(copy);
      return copy;
   }
}
