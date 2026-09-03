package net.azisaba.lifemoremythicmobs.targeters;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.core.skills.targeters.IEntitySelector;
import io.lumine.mythic.core.utils.annotations.MythicTargeter;
import java.util.HashSet;

@MythicTargeter(author = "igachi77", name = "livingInRadiusCustom", aliases = {"entitiesNearOriginCustom", "ENOC"})
public class EntitiesNearOriginCustomTargeter extends IEntitySelector {
   private PlaceholderDouble radius;

   public EntitiesNearOriginCustomTargeter(SkillExecutor executor, MythicLineConfig config) {
      super(executor, config);
      this.radius = PlaceholderDouble.of(config.getString(new String[]{"radius", "r"}, "5.0F", new String[0]));
   }

   public HashSet<AbstractEntity> getEntities(SkillMetadata data) {
      double radius = this.radius.get(data);
      HashSet<AbstractEntity> targets = new HashSet<>();

      for (org.bukkit.entity.LivingEntity living : BukkitAdapter.adapt(data.getOrigin()).getWorld().getLivingEntities()) {
         AbstractEntity p = BukkitAdapter.adapt(living);
         if (data.getCaster().getLocation().getWorld().equals(p.getWorld())
            && !p.getUniqueId().equals(data.getCaster().getEntity().getUniqueId())
            && data.getOrigin().distanceSquared(p.getLocation()) < Math.pow(radius, 2.0)) {
            targets.add(p);
         }
      }

      return targets;
   }
}

