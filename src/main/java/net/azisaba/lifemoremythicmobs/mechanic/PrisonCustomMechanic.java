package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class PrisonCustomMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final Material material;
   private final int duration;
   private final int radius;
   private static final Map<UUID, Map<Material, PrisonCustomMechanic.PrisonTask>> activePrisons = new ConcurrentHashMap<>();

   public PrisonCustomMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.material = Material.valueOf(config.getString("material", "GLASS").toUpperCase());
      this.duration = config.getInteger("duration", 100);
      this.radius = config.getInteger("radius", 1);
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      Location original = target.getBukkitEntity().getLocation();
      int x = original.getBlockX();
      int y = original.getBlockY();
      int z = original.getBlockZ();
      UUID uuid = target.getUniqueId();
      PrisonCustomMechanic.PrisonTask existing = activePrisons.getOrDefault(uuid, new HashMap<>()).get(this.material);
      if (existing != null) {
         existing.extend(this.duration);
         return SkillResult.SUCCESS;
      }

      double tpX = x < 0 ? x - 0.5 : x + 0.5;
      double tpY = y + 0.5;
      double tpZ = z < 0 ? z - 0.5 : z + 0.5;
      Location center = new Location(original.getWorld(), tpX, tpY, tpZ, original.getYaw(), original.getPitch());
      target.getBukkitEntity().teleport(center);
      Entity bukkitEntity = BukkitAdapter.adapt(target);
      Block feet = bukkitEntity.getLocation().getBlock();
      Set<Block> prisonBlocks = new HashSet<>();
      int[][] offsets = new int[][]{{1, 0, 0}, {1, 1, 0}, {-1, 0, 0}, {-1, 1, 0}, {0, 0, 1}, {0, 1, 1}, {0, 0, -1}, {0, 1, -1}, {0, -1, 0}, {0, 2, 0}};
      int[][] var23 = offsets;
      int var22 = offsets.length;

      for (int var21 = 0; var21 < var22; var21++) {
         int[] offset = var23[var21];
         Block block = feet.getRelative(offset[0], offset[1], offset[2]);
         if (block.getType() == Material.AIR) {
            block.setType(this.material);
            prisonBlocks.add(block);
         }
      }

      PrisonCustomMechanic.PrisonTask task = new PrisonCustomMechanic.PrisonTask(prisonBlocks, uuid, this.material);
      activePrisons.computeIfAbsent(uuid, k -> new HashMap<>()).put(this.material, task);
      task.start(this.duration);
      return SkillResult.SUCCESS;
   }

   private static class PrisonTask {
      private final Set<Block> blocks;
      private final UUID target;
      private final Material material;
      private BukkitRunnable runnable;

      public PrisonTask(Set<Block> blocks, UUID target, Material material) {
         this.blocks = blocks;
         this.target = target;
         this.material = material;
      }

      public void start(int ticks) {
         this.runnable = new BukkitRunnable() {
            public void run() {
               for (Block block : PrisonTask.this.blocks) {
                  if (block.getType() == PrisonTask.this.material) {
                     block.setType(Material.AIR);
                  }
               }

               Map<Material, PrisonCustomMechanic.PrisonTask> prisons = PrisonCustomMechanic.activePrisons.get(PrisonTask.this.target);
               if (prisons != null) {
                  prisons.remove(PrisonTask.this.material);
                  if (prisons.isEmpty()) {
                     PrisonCustomMechanic.activePrisons.remove(PrisonTask.this.target);
                  }
               }
            }
         };
         this.runnable.runTaskLater(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), ticks);
      }

      public void extend(int extraTicks) {
         this.runnable.cancel();
         this.start(extraTicks);
      }
   }
}
