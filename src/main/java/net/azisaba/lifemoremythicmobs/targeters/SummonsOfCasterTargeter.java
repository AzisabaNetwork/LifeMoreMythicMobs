package net.azisaba.lifemoremythicmobs.targeters;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.mobs.ActiveMob;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.targeters.IEntitySelector;
import java.util.Optional;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

public class SummonsOfCasterTargeter extends IEntitySelector {
   public SummonsOfCasterTargeter(MythicLineConfig config) {
      super(config);
   }

   public HashSet<AbstractEntity> getEntities(SkillMetadata data) {
      HashSet<AbstractEntity> targets = new HashSet<>();
      AbstractEntity caster = data.getCaster().getEntity();
      ActiveMob casterMob = MythicBukkit.inst().getMobManager().getMythicMobInstance(caster);
      UUID casterUUID;
      if (casterMob != null) {
         casterUUID = casterMob.getUniqueId();
      } else {
         casterUUID = caster.getUniqueId();
      }

      Location origin = BukkitAdapter.adapt(caster.getLocation());

      for (Entity entity : origin.getWorld()
         .getNearbyEntities(origin, 60.0, 60.0, 60.0)
         .stream()
         .filter(e -> e instanceof LivingEntity)
         .collect(Collectors.toList())) {
         AbstractEntity abstractEntity = BukkitAdapter.adapt(entity);
         ActiveMob mob = (ActiveMob)MythicBukkit.inst().getMobManager().getActiveMob(abstractEntity.getUniqueId()).orElse(null);
         if (mob != null) {
            Optional<UUID> ownerOpt = mob.getOwner();
            ownerOpt.isPresent();
            if (ownerOpt.isPresent() && ((UUID)ownerOpt.get()).equals(casterUUID)) {
               targets.add(abstractEntity);
            }
         }
      }

      return targets;
   }

   private void log(String msg) {
      ((LifeMoreMythicMobs)JavaPlugin.getPlugin(LifeMoreMythicMobs.class)).getLogger().info("[SummonsOfCasterTargeter] " + msg);
   }
}
