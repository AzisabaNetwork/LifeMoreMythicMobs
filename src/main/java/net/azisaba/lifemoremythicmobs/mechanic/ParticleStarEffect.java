package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractVector;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.AbstractSkill.ThreadSafetyLevel;
import io.lumine.mythic.api.skills.ParticleMaker.ParticlePacket;
import io.lumine.mythic.api.skills.mechanics.ParticleEffect;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.utils.Schedulers;
import io.lumine.mythic.utils.numbers.Numbers;
import io.lumine.mythic.utils.version.MinecraftVersions;
import io.lumine.mythic.utils.version.ServerVersion;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParticleStarEffect extends ParticleEffect implements ITargetedEntitySkill, ITargetedLocationSkill {
   private static final Logger log = LoggerFactory.getLogger(ParticleStarEffect.class);
   private final PlaceholderDouble radius;
   private final PlaceholderDouble pointInterval;
   private final PlaceholderDouble offsetX;
   private final PlaceholderDouble offsetY;
   private final PlaceholderDouble offsetZ;
   private final PlaceholderDouble rotXDeg;
   private final PlaceholderDouble rotYDeg;
   private final PlaceholderDouble rotZDeg;

   public ParticleStarEffect(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.threadSafetyLevel = ThreadSafetyLevel.ASYNC_ONLY;
      this.radius = PlaceholderDouble.of(config.getString(new String[]{"radius", "r"}, "1.0", new String[0]));
      this.pointInterval = PlaceholderDouble.of(config.getString(new String[]{"pointinterval", "interval", "step"}, "0.2", new String[0]));
      this.offsetX = PlaceholderDouble.of(config.getString(new String[]{"offsetx", "ox"}, "0.0", new String[0]));
      this.offsetY = PlaceholderDouble.of(config.getString(new String[]{"offsety", "oy"}, "0.0", new String[0]));
      this.offsetZ = PlaceholderDouble.of(config.getString(new String[]{"offsetz", "oz"}, "0.0", new String[0]));
      this.rotXDeg = PlaceholderDouble.of(config.getString(new String[]{"rotx", "rx"}, "0.0", new String[0]));
      this.rotYDeg = PlaceholderDouble.of(config.getString(new String[]{"roty", "ry", "rotation", "rot", "yawoffset"}, "0.0", new String[0]));
      this.rotZDeg = PlaceholderDouble.of(config.getString(new String[]{"rotz", "rz"}, "0.0", new String[0]));
   }

   public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
      Collection<AbstractEntity> audience = this.audience.get(data, null);
      this.playParticleStarEffect(data, target, audience);
      return SkillResult.SUCCESS;
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      Collection<AbstractEntity> audience = this.audience.get(data, target);
      this.playParticleStarEffect(data, target.getLocation(), audience);
      return SkillResult.SUCCESS;
   }

   private void playParticleStarEffect(SkillMetadata data, AbstractLocation center, Collection<AbstractEntity> audience) {
      if (audience != null && !audience.isEmpty()) {
         AbstractLocation base = center.clone();
         if (this.setYaw) {
            base.setYaw(this.yaw);
         }

         if (this.setPitch) {
            base.setPitch(this.pitch);
         }

         double r = Math.max(0.0, this.radius.get(data));
         if (!(r <= 0.0)) {
            double interval = this.pointInterval.get(data);
            if (interval <= 0.0) {
               interval = 0.2;
            }

            double offX = this.offsetX.get(data);
            double offY = this.offsetY.get(data);
            double offZ = this.offsetZ.get(data);
            base.add(offX, offY, offZ);
            double rotX = Math.toRadians(this.rotXDeg.get(data));
            double rotY = Math.toRadians(this.rotYDeg.get(data));
            double rotZ = Math.toRadians(this.rotZDeg.get(data));
            AbstractLocation[] vertices = new AbstractLocation[5];

            for (int i = 0; i < 5; i++) {
               double angle = (Math.PI * 2) * i / 5.0;
               double lx = r * Math.cos(angle);
               double ly = 0.0;
               double lz = r * Math.sin(angle);
               double x1 = lx;
               double y1 = ly * Math.cos(rotX) - lz * Math.sin(rotX);
               double z1 = ly * Math.sin(rotX) + lz * Math.cos(rotX);
               double x2 = x1 * Math.cos(rotY) + z1 * Math.sin(rotY);
               double y2 = y1;
               double z2 = -x1 * Math.sin(rotY) + z1 * Math.cos(rotY);
               double x3 = x2 * Math.cos(rotZ) - y2 * Math.sin(rotZ);
               double y3 = x2 * Math.sin(rotZ) + y2 * Math.cos(rotZ);
               double z3 = z2;
               AbstractLocation v = base.clone().add(x3, y3, z3);
               vertices[i] = v;
            }

            int[] order = new int[]{0, 2, 4, 1, 3, 0};

            for (int i = 0; i < order.length - 1; i++) {
               AbstractLocation from = vertices[order[i]];
               AbstractLocation to = vertices[order[i + 1]];
               this.drawLine(data, from, to, interval, audience);
            }
         }
      }
   }

   private void drawLine(SkillMetadata data, AbstractLocation from, AbstractLocation to, double interval, Collection<AbstractEntity> audience) {
      AbstractVector dir = to.clone().toVector().subtract(from.clone().toVector());
      double length = dir.length();
      if (!(length <= 0.0)) {
         AbstractVector step = dir.normalize().multiply(interval);
         int steps = (int)Math.floor(length / interval);
         AbstractLocation current = from.clone();

         for (int i = 0; i <= steps; i++) {
            if (this.directional) {
               this.playDirectionalParticleEffect(data, from, to, current, audience);
            } else {
               this.playParticleCustomEffect(data, current, audience);
            }

            current.add(step);
         }

         if (steps * interval < length) {
            if (this.directional) {
               this.playDirectionalParticleEffect(data, from, to, to, audience);
            } else {
               this.playParticleCustomEffect(data, to, audience);
            }
         }
      }
   }

   private void playDirectionalParticleEffect(
      SkillMetadata data, AbstractLocation origin, AbstractLocation target, AbstractLocation spawn, Collection<AbstractEntity> audience
   ) {
      AbstractVector direction;
      if (this.directionReversed) {
         direction = origin.toVector().subtract(target.clone().toVector().normalize());
      } else {
         direction = target.toVector().subtract(origin.clone().toVector().normalize());
      }

      if (ServerVersion.isAfterOrEq(MinecraftVersions.v1_15)) {
         if (this.particleData == null) {
            this.particle.sendDirectional(audience, origin, this.pSpeed, 1, this.xSpread, this.vSpread, this.zSpread, direction);
         }
      } else {
         AbstractLocation ln = spawn.clone()
            .add(
               0.0F - this.xSpread + Numbers.randomDouble() * this.xSpread * 2.0,
               this.vSpread + Numbers.randomDouble() * this.vSpread * 2.0,
               (0.0F - this.zSpread) * Numbers.randomDouble() * this.zSpread * 2.0
            );
         new ParticlePacket(this.strParticle, direction, this.pSpeed, 1, true).send(ln, this.viewDistance);
      }
   }

   private void playParticleCustomEffect(SkillMetadata data, AbstractLocation target, Collection<AbstractEntity> audience) {
      AbstractLocation ln = target.clone().add(0.0, this.yOffset, 0.0);
      if (!this.isMob) {
         if (ServerVersion.isAfterOrEq(MinecraftVersions.v1_15)) {
            if (this.particleData == null) {
               if (this.color != null) {
                  this.particle.sendLegacyColored(audience, ln, this.pSpeed, this.amount.get(data), this.xSpread, this.vSpread, this.zSpread, this.color);
               } else {
                  this.particle.send(audience, ln, this.pSpeed, this.amount.get(data), this.xSpread, this.vSpread, this.zSpread);
               }
            } else {
               this.particle
                  .send(audience, ln, this.pSpeed, this.amount.get(data), this.xSpread, this.vSpread, this.zSpread, this.particleData, this.exactOffsets);
            }
         } else {
            if (this.color != null) {
               this.playColoredParticleEffect(data, target, audience);
               return;
            }

            new ParticlePacket(this.strParticle, this.xSpread, this.vSpread, this.zSpread, this.pSpeed, this.amount.get(data), true)
               .send(ln, this.viewDistance);
         }
      } else {
         ln.add(
            Math.random() * this.xSpread * 2.0 - this.xSpread,
            Math.random() * this.vSpread * 2.0 - this.vSpread,
            Math.random() * this.zSpread * 2.0 - this.zSpread
         );
         Schedulers.sync().run(() -> MythicBukkit.inst().getMobManager().spawnMob(this.strMob, ln));
      }
   }
}
