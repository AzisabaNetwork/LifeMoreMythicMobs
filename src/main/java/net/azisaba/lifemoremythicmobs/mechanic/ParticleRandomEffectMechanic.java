package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.mechanics.ParticleEffect;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import java.util.Collection;
import java.util.Random;
import org.bukkit.Location;

public class ParticleRandomEffectMechanic extends ParticleEffect {
   private final PlaceholderInt points;
   private final PlaceholderDouble range;
   private final PlaceholderDouble yOffset;
   private final Random random = new Random();

   public ParticleRandomEffectMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.points = config.getPlaceholderInteger("points", 10);
      this.range = config.getPlaceholderDouble("range", 1.0);
      this.yOffset = config.getPlaceholderDouble("yOffset", 0.0);
   }

   public void playEffect(SkillMetadata data, AbstractLocation origin) {
      Location center = BukkitAdapter.adapt(origin);
      center.add(0.0, this.yOffset.get(data), 0.0);
      int pointCount = this.points.get(data);
      double r = this.range.get(data);
      AbstractEntity caster = data.getCaster().getEntity();
      Collection<AbstractEntity> audienceEntities = this.audience.get(data, caster);

      for (int i = 0; i < pointCount; i++) {
         double dx = (this.random.nextDouble() * 2.0 - 1.0) * r;
         double dy = (this.random.nextDouble() * 2.0 - 1.0) * r;
         double dz = (this.random.nextDouble() * 2.0 - 1.0) * r;
         Location loc = center.clone().add(dx, dy, dz);
         AbstractLocation abstractLoc = BukkitAdapter.adapt(loc);
         this.playParticleEffect(data, abstractLoc, audienceEntities);
      }
   }
}
