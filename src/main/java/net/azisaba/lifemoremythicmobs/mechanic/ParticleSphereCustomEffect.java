package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.core.skills.SkillExecutor;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.api.adapters.AbstractVector;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import io.lumine.mythic.core.skills.ParticleMaker.ParticlePacket;
import io.lumine.mythic.core.skills.mechanics.ParticleEffect;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.core.utils.RandomUtil;
import io.lumine.mythic.bukkit.utils.Schedulers;
import io.lumine.mythic.bukkit.utils.numbers.Numbers;
import io.lumine.mythic.bukkit.utils.version.MinecraftVersions;
import io.lumine.mythic.bukkit.utils.version.ServerVersion;
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
      Collection<AbstractPlayer> audience = this.audience.get(data, null);
      this.playParticleSphereEffect(data, target, audience);
      return SkillResult.SUCCESS;
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      Collection<AbstractPlayer> audience = this.audience.get(data, target);
      this.playParticleSphereEffect(data, target.getLocation(), audience);
      return SkillResult.SUCCESS;
   }

   private void playParticleSphereEffect(SkillMetadata data, AbstractLocation t, Collection<AbstractPlayer> audience) {
      AbstractLocation target = t;
      AbstractLocation location = t.clone();
      if (this.setYaw) {
         location.setYaw(this.yaw);
      }

      if (this.setPitch) {
         location.setPitch(this.pitch);
      }

      location.add(0.0, this.yOffset.get(data), 0.0);
      int points = this.points.get(data);

      for (int i = 0; i < points; i++) {
         AbstractVector vector = RandomUtil.getRandomVector().multiply(this.radius.get(data));
         location.add(vector);
         super.playParticleEffect(data, location, audience);

         location.subtract(vector);
      }
   }

}



