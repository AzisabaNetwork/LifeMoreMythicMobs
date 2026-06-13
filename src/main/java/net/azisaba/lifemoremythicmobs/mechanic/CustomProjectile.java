package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.projectiles.Projectile;
import io.lumine.mythic.api.skills.projectiles.Projectile.ProjectileTracker;

public class CustomProjectile extends Projectile {
   private final CustomProjectileMechanic mechanic;
   private final String pathType;
   private final String sineAxis;
   private final float amplitude;
   private final float frequency;
   private final SkillMetadata data;
   private final AbstractLocation target;

   public CustomProjectile(CustomProjectileMechanic mechanic, MythicLineConfig config, SkillMetadata data, AbstractLocation target) {
      super(config.getLine(), config);
      this.mechanic = mechanic;
      this.data = data;
      this.target = target;
      this.pathType = config.getString("path", "linear").toLowerCase();
      this.sineAxis = config.getString("axis", "y").toLowerCase();
      this.amplitude = config.getFloat("amplitude", 1.0F);
      this.frequency = config.getFloat("frequency", 1.0F);
   }

   public ProjectileTracker createCustomTracker(SkillMetadata data, AbstractLocation target) {
      return this.mechanic.new CustomProjectileTracker(this.mechanic, data, target);
   }
}
