package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

public class SpherePlaceMechanic extends SkillMechanic implements ITargetedEntitySkill, ITargetedLocationSkill {
   private final Material material;
   private final PlaceholderDouble radius;
   private final PlaceholderInt duration;
   private final JavaPlugin plugin = JavaPlugin.getPlugin(LifeMoreMythicMobs.class);

   public SpherePlaceMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      String matName = config.getString("material", "STONE").toUpperCase();
      this.material = Material.matchMaterial(matName);
      this.radius = PlaceholderDouble.of(config.getString("radius", "3.0"));
      this.duration = PlaceholderInt.of(config.getString("duration", "100"));
      if (this.material == null) {
         IgaDebugLogger.log(this.getClass(), "Invalid material: " + matName);
      }
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      if (this.material != null && this.material.isBlock()) {
         Location center = target.getBukkitEntity().getLocation();
         this.placeSphereAndScheduleRemoval(data, center);
         return SkillResult.SUCCESS;
      } else {
         return SkillResult.FAILURE;
      }
   }

   public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
      if (this.material != null && this.material.isBlock()) {
         Location center = BukkitAdapter.adapt(target);
         this.placeSphereAndScheduleRemoval(data, center);
         return SkillResult.SUCCESS;
      } else {
         return SkillResult.FAILURE;
      }
   }

   private void placeSphereAndScheduleRemoval(SkillMetadata data, Location center) {
      double radius = this.radius.get(data);
      int intRadius = (int)Math.ceil(radius);
      int duration = this.duration.get(data);
      Bukkit.getScheduler().runTask(this.plugin, () -> {
         try {
            Set<Block> changedBlocks = new HashSet<>();
            int totalSet = 0;

            for (int x = -intRadius; x <= intRadius; x++) {
               for (int y = -intRadius; y <= intRadius; y++) {
                  for (int z = -intRadius; z <= intRadius; z++) {
                     if (x * x + y * y + z * z <= radius * radius) {
                        Block block = center.clone().add(x, y, z).getBlock();
                        if (block.getType() == Material.AIR) {
                           block.setType(this.material, false);
                           changedBlocks.add(block);
                           totalSet++;
                        }
                     }
                  }
               }
            }

            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
               int removed = 0;

               for (Block blockx : changedBlocks) {
                  if (blockx.getType() == this.material) {
                     blockx.setType(Material.AIR, false);
                     removed++;
                  }
               }
            }, duration);
         } catch (Exception e) {
            IgaDebugLogger.log(this.getClass(), "Sync task error: " + e.getMessage());
            e.printStackTrace();
         }
      });
   }

   private String fmt(Location l) {
      return String.format("%.3f,%.3f,%.3f (%s)", l.getX(), l.getY(), l.getZ(), l.getWorld() != null ? l.getWorld().getName() : "null");
   }
}
