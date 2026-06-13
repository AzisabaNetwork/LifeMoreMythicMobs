package net.azisaba.lifemoremythicmobs.targeters;

import com.google.common.collect.Sets;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.targeters.IEntitySelector;
import io.lumine.mythic.util.annotations.MythicTargeter;
import java.util.HashSet;
import java.util.Locale;
import java.util.logging.Logger;

@MythicTargeter(
   author = "igachi77",
   name = "livingInRadiusCustom",
   aliases = {"livingEntitiesInRadiusCustom", "entitiesInRadiusCustom", "EIRC"},
   description = "Extended EntitiesInRadius: can center on origin and optionally include self"
)
public class LivingInRadiusCustomTargeter extends IEntitySelector {
   private static final Logger LOGGER = MythicBukkit.inst().getLogger();
   private static final String PREFIX = "[LivingInRadiusCustom]";
   private final PlaceholderDouble radius;
   private final boolean useOrigin;
   private final boolean includeSelf;

   public LivingInRadiusCustomTargeter(MythicLineConfig config) {
      super(config);
      String radiusStr = config.getString(new String[]{"radius", "r"}, "5.00", new String[0]);
      this.radius = PlaceholderDouble.of(radiusStr);
      this.useOrigin = config.getBoolean(new String[]{"origin"}, false);
      String target = config.getString("target", null);
      boolean targetSelfFlag = false;
      if (target != null) {
         String lower = target.toLowerCase(Locale.ROOT);
         if (lower.contains("self") || lower.contains("caster")) {
            targetSelfFlag = true;
         }
      }

      targetSelfFlag = config.getBoolean("targetself", targetSelfFlag);
      this.includeSelf = targetSelfFlag;
      LOGGER.info(
         "[LivingInRadiusCustom] constructed. rawRadius=\""
            + radiusStr
            + "\", useOrigin="
            + this.useOrigin
            + ", includeSelf="
            + this.includeSelf
            + " (target=\""
            + target
            + "\", targetself="
            + config.getString("targetself", "null")
            + ")"
      );
   }

   public HashSet<AbstractEntity> getEntities(SkillMetadata data) {
      double radius = this.radius.get(data);
      SkillCaster caster = data.getCaster();
      HashSet<AbstractEntity> targets = Sets.newHashSet();
      AbstractLocation center = this.useOrigin ? data.getOrigin() : caster.getLocation();
      LOGGER.info(
         "[LivingInRadiusCustom]getEntities called. caster="
            + this.safeName(caster.getEntity())
            + ", world="
            + (center != null ? center.getWorld().getName() : "null")
            + ", center=("
            + (center != null ? this.formatLoc(center) : "null")
            + "), radius="
            + radius
            + ", useOrigin="
            + this.useOrigin
            + ", includeSelf="
            + this.includeSelf
      );
      int considered = 0;
      int skippedSelf = 0;

      for (AbstractEntity p : getPlugin().getVolatileCodeHandler().getWorldHandler().getEntitiesNearLocation(center, radius, AbstractEntity::isLiving)) {
         considered++;
         boolean isSelf = p.getUniqueId().equals(caster.getEntity().getUniqueId());
         if (!this.includeSelf && isSelf) {
            skippedSelf++;
            LOGGER.info("[LivingInRadiusCustom]  candidate=" + this.safeName(p) + " skipped (self and includeSelf=false)");
         } else {
            double distSq = center.distanceSquared(p.getLocation());
            LOGGER.info("[LivingInRadiusCustom]  candidate=" + this.safeName(p) + ", isSelf=" + isSelf + ", distSq=" + distSq);
            if (center.getWorld().equals(p.getWorld()) && center.distanceSquared(p.getLocation()) < radius * radius) {
               LOGGER.info("[LivingInRadiusCustom]    -> added to targets");
               targets.add(p);
            } else {
               LOGGER.info("[LivingInRadiusCustom]    -> NOT added (world or distance check failed)");
            }
         }
      }

      LOGGER.info("[LivingInRadiusCustom]getEntities finished. considered=" + considered + ", skippedSelf=" + skippedSelf + ", resultSize=" + targets.size());
      return targets;
   }

   private String safeName(AbstractEntity ae) {
      if (ae == null) {
         return "null";
      }

      try {
         return ae.getName();
      } catch (Exception e) {
         return ae.getUniqueId().toString();
      }
   }

   private String formatLoc(AbstractLocation loc) {
      return String.format(Locale.ROOT, "%.2f, %.2f, %.2f", loc.getX(), loc.getY(), loc.getZ());
   }
}
