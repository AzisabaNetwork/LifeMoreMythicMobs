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
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.util.RandomUtil;
import io.lumine.mythic.utils.Schedulers;
import io.lumine.mythic.utils.numbers.Numbers;
import io.lumine.mythic.utils.version.MinecraftVersions;
import io.lumine.mythic.utils.version.ServerVersion;
import java.util.Collection;

public class ParticleSphereCustomEffect extends ParticleEffect implements ITargetedEntitySkill, ITargetedLocationSkill {
   private PlaceholderInt points;
   private PlaceholderDouble radius;

   public ParticleSphereCustomEffect(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.threadSafetyLevel = ThreadSafetyLevel.ASYNC_ONLY;
      this.points = this.amount;
      this.amount = PlaceholderInt.of("1");
      this.radius = PlaceholderDouble.of(config.getString(new String[]{"radius", "r"}, "0.0", new String[0]));
   }

   public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
      Collection<AbstractEntity> audience = this.audience.get(data, null);
      this.playParticleSphereEffect(data, target, audience);
      return SkillResult.SUCCESS;
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      Collection<AbstractEntity> audience = this.audience.get(data, target);
      this.playParticleSphereEffect(data, target.getLocation(), audience);
      return SkillResult.SUCCESS;
   }

   private void playParticleSphereEffect(SkillMetadata data, AbstractLocation t, Collection<AbstractEntity> audience) {
      AbstractLocation target = t;
      AbstractLocation location = t.clone();
      if (this.setYaw) {
         location.setYaw(this.yaw);
      }

      if (this.setPitch) {
         location.setPitch(this.pitch);
      }

      location.add(0.0, this.yOffset, 0.0);
      int points = this.points.get(data);

      for (int i = 0; i < points; i++) {
         AbstractVector vector = RandomUtil.getRandomVector().multiply(this.radius.get(data));
         location.add(vector);
         if (this.directional) {
            this.playDirectionalParticleEffect(data, target, target, location, audience);
         } else {
            this.playParticleCustomEffect(data, location, audience);
         }

         location.subtract(vector);
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
               0.0F - this.zSpread + Numbers.randomDouble() * this.zSpread * 2.0
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
