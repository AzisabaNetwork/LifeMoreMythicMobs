package net.azisaba.lifemoremythicmobs.conditions;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.compatibility.WorldGuardSupport;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.entity.Entity;

public class WorldNotInConfigCondition extends SkillCondition implements IEntityCondition {
   private final Set<String> excludeWorldsLower;
   private final Set<String> excludeRegionsLower;
   private final boolean log;

   public WorldNotInConfigCondition(String line, MythicLineConfig config) {
      super(line);
      String exWorldsRaw = config.getString(new String[]{"exclude", "ex", "ignore", "worlds"}, "", new String[0]);
      this.excludeWorldsLower = splitToLowerSet(exWorldsRaw);
      String exRegionsRaw = config.getString(new String[]{"excluderegions", "exregions", "rex", "er", "ignoreregions"}, "", new String[0]);
      this.excludeRegionsLower = splitToLowerSet(exRegionsRaw);
      this.log = config.getBoolean(new String[]{"log", "debug"}, false);
   }

   public boolean check(AbstractEntity entity) {
      Set<String> configWorlds = JavaPlugin.getPlugin(LifeMoreMythicMobs.class).getConfigService().getConfiguredWorldsLower();
      Set<String> configRegions = JavaPlugin.getPlugin(LifeMoreMythicMobs.class).getConfigService().getConfiguredRegionsLower();
      if (configWorlds.isEmpty() && configRegions.isEmpty()) {
         if (this.log) {
            this.log("no worlds/regions configured -> FALSE");
         }

         return false;
      } else {
         Entity bukkit = entity.getBukkitEntity();
         String currentWorld = bukkit.getWorld().getName().toLowerCase(Locale.ROOT);
         boolean worldMatched = configWorlds.contains(currentWorld) && !this.excludeWorldsLower.contains(currentWorld);
         if (worldMatched) {
            if (this.log) {
               this.log("world matched -> FALSE (world=" + currentWorld + ")");
            }

            return false;
         } else if (configRegions.isEmpty()) {
            if (this.log) {
               this.log("world not matched & no regions configured -> TRUE");
            }

            return true;
         } else {
            Optional<?> wgOpt = MythicBukkit.inst().getCompatibility().getWorldGuard();
            if (!wgOpt.isPresent()) {
               if (this.log) {
                  this.log("world not matched & WorldGuard absent -> TRUE");
               }

               return true;
            } else {
               WorldGuardSupport wg = (WorldGuardSupport)wgOpt.get();
               AbstractLocation loc = BukkitAdapter.adapt(bukkit.getLocation());

               for (String regionNameLower : configRegions) {
                  if (this.excludeRegionsLower.contains(regionNameLower)) {
                     if (this.log) {
                        this.log("skip excluded region=" + regionNameLower);
                     }
                  } else {
                     boolean inThisRegion = wg.isLocationInRegions(loc, regionNameLower);
                     if (inThisRegion) {
                        if (this.log) {
                           this.log("world not matched, BUT in region=" + regionNameLower + " -> FALSE");
                        }

                        return false;
                     }
                  }
               }

               if (this.log) {
                  this.log("world not matched & not in any region -> TRUE");
               }

               return true;
            }
         }
      }
   }

   private static Set<String> splitToLowerSet(String raw) {
      return raw != null && !raw.trim().isEmpty()
         ? Arrays.stream(raw.split("[,;\\s]+"))
            .filter(s -> !s.isEmpty())
            .map(s -> s.toLowerCase(Locale.ROOT))
            .collect(Collectors.toCollection(LinkedHashSet::new))
         : Collections.emptySet();
   }

   private void log(String s) {
      IgaDebugLogger.log(this.getClass(), s);
   }
}
