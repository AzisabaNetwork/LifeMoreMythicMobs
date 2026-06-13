package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractVector;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.AbstractSkill.ThreadSafetyLevel;
import io.lumine.mythic.api.skills.mechanics.ParticleEffect;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.util.VectorUtils;
import io.lumine.mythic.utils.tasks.Scheduler;
import io.lumine.mythic.utils.tasks.Scheduler.Task;
import java.util.Collection;
import java.util.Optional;

public class ParticleOrbitalCustomEffect extends ParticleEffect implements ITargetedEntitySkill, ITargetedLocationSkill {
   protected float radius;
   protected int points;
   protected int interval;
   protected int iterations;
   protected double velocity = 1.0;
   protected boolean rotate = false;
   protected boolean reversed = false;
   protected double xRotation = 0.0;
   protected double yRotation = 0.0;
   protected double zRotation = 0.0;
   protected double angularVelocityX = 0.015707963267948967;
   protected double angularVelocityY = 0.018479956785822312;
   protected double angularVelocityZ = 0.02026833970057931;
   protected double xOffset = 0.0;
   protected double yOffset = 0.0;
   protected double zOffset = 0.0;
   protected PlaceholderDouble startAngleDeg;

   public ParticleOrbitalCustomEffect(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.threadSafetyLevel = ThreadSafetyLevel.ASYNC_ONLY;
      this.radius = config.getFloat(new String[]{"radius", "r"}, 4.0F);
      this.points = config.getInteger(new String[]{"points", "p"}, 20);
      this.iterations = config.getInteger(new String[]{"ticks", "t"}, 100);
      this.interval = config.getInteger(new String[]{"interval", "in", "i"}, 10);
      this.xRotation = config.getDouble(new String[]{"rotationx", "rotx", "rx"}, 0.0);
      this.yRotation = config.getDouble(new String[]{"rotationy", "roty", "ry"}, 0.0);
      this.zRotation = config.getDouble(new String[]{"rotationz", "rotz", "rz"}, 0.0);
      this.xOffset = config.getDouble(new String[]{"offsetx", "offx", "ox"}, 0.0);
      this.yOffset = config.getDouble(new String[]{"offsety", "offy", "oy"}, 0.0);
      this.zOffset = config.getDouble(new String[]{"offsetz", "offz", "oz"}, 0.0);
      this.angularVelocityX = config.getDouble(new String[]{"angularvelocityx", "avx", "vx"}, 0.0);
      this.angularVelocityY = config.getDouble(new String[]{"angularvelocityy", "avy", "vy"}, 0.0);
      this.angularVelocityZ = config.getDouble(new String[]{"angularvelocityz", "avz", "vz"}, 0.0);
      this.angularVelocityX = this.angularVelocityX == 0.0 ? 0.0 : Math.PI / this.angularVelocityX;
      this.angularVelocityY = this.angularVelocityY == 0.0 ? 0.0 : Math.PI / this.angularVelocityY;
      this.angularVelocityZ = this.angularVelocityZ == 0.0 ? 0.0 : Math.PI / this.angularVelocityZ;
      this.rotate = config.getBoolean(new String[]{"rotate"}, this.angularVelocityX > 0.0 || this.angularVelocityY > 0.0 || this.angularVelocityZ > 0.0);
      this.reversed = config.getBoolean(new String[]{"reversed", "reverse"}, false);
      this.startAngleDeg = PlaceholderDouble.of(config.getString(new String[]{"startangle", "sa"}, "0", new String[0]));
   }

   public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
      new ParticleOrbitalCustomEffect.Animator(data, target);
      return SkillResult.SUCCESS;
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      new ParticleOrbitalCustomEffect.Animator(data, target);
      return SkillResult.SUCCESS;
   }

   private class Animator implements Runnable {
      private SkillMetadata data;
      private Optional<AbstractEntity> entity = Optional.empty();
      private Optional<AbstractLocation> location = Optional.empty();
      private int iteration = 0;
      private int step = 0;
      private Task task;

      public Animator(SkillMetadata data, AbstractEntity entity) {
         this.data = data;
         this.entity = Optional.of(entity);
         this.task = Scheduler.runTaskRepeatingAsync(this, 0L, ParticleOrbitalCustomEffect.this.interval);
      }

      public Animator(SkillMetadata data, AbstractLocation location) {
         this.data = data;
         this.location = Optional.of(location);
         this.task = Scheduler.runTaskRepeatingAsync(this, 0L, ParticleOrbitalCustomEffect.this.interval);
      }

      public AbstractLocation getLocation() {
         return this.entity.isPresent()
            ? this.entity
               .get()
               .getLocation()
               .add(ParticleOrbitalCustomEffect.this.xOffset, ParticleOrbitalCustomEffect.this.yOffset, ParticleOrbitalCustomEffect.this.zOffset)
            : this.location
               .get()
               .clone()
               .add(ParticleOrbitalCustomEffect.this.xOffset, ParticleOrbitalCustomEffect.this.yOffset, ParticleOrbitalCustomEffect.this.zOffset);
      }

      @Override
      public void run() {
         AbstractLocation location = this.getLocation();
         double inc = (Math.PI * 2) / ParticleOrbitalCustomEffect.this.points;
         double angle = Math.toRadians(ParticleOrbitalCustomEffect.this.startAngleDeg.get(this.data)) + this.step * inc;
         AbstractVector v = new AbstractVector();
         v.setX(Math.cos(angle) * ParticleOrbitalCustomEffect.this.radius);
         v.setZ(Math.sin(angle) * ParticleOrbitalCustomEffect.this.radius);
         VectorUtils.rotateVector(
            v, ParticleOrbitalCustomEffect.this.xRotation, ParticleOrbitalCustomEffect.this.yRotation, ParticleOrbitalCustomEffect.this.zRotation
         );
         if (ParticleOrbitalCustomEffect.this.rotate) {
            VectorUtils.rotateVector(
               v,
               ParticleOrbitalCustomEffect.this.angularVelocityX * this.step,
               ParticleOrbitalCustomEffect.this.angularVelocityY * this.step,
               ParticleOrbitalCustomEffect.this.angularVelocityZ * this.step
            );
         }

         AbstractLocation loc = ParticleOrbitalCustomEffect.this.reversed ? location.subtract(v) : location.add(v);
         Collection<AbstractEntity> audience = ParticleOrbitalCustomEffect.this.audience.get(this.data, this.data.getCaster().getEntity());
         ParticleOrbitalCustomEffect.this.playParticleEffect(this.data, loc, audience);
         this.step++;
         if (++this.iteration * ParticleOrbitalCustomEffect.this.interval >= ParticleOrbitalCustomEffect.this.iterations) {
            this.task.terminate();
         }
      }
   }
}
