package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class HealBlockMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final PlaceholderDouble durationTicks;
   private final boolean refresh;
   private final boolean resetOnRefresh;
   private final boolean log;
   private static final Map<UUID, HealBlockMechanic.Active> ACTIVE = new ConcurrentHashMap<>();
   private static volatile boolean bootstrapDone = false;
   private static BukkitTask clampTask;

   public HealBlockMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.durationTicks = PlaceholderDouble.of(config.getString(new String[]{"duration", "d"}, "100.0D", new String[0]));
      this.refresh = config.getBoolean(new String[]{"refresh", "r"}, true);
      this.resetOnRefresh = config.getBoolean(new String[]{"resetonrefresh", "resetCap", "reset"}, true);
      this.log = config.getBoolean(new String[]{"log"}, false);
      ensureBootstrap(this.log);
   }

   private static void ensureBootstrap(boolean log) {
      if (!bootstrapDone) {
         synchronized (HealBlockMechanic.class) {
            if (!bootstrapDone) {
               clampTask = Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), () -> {
                  long now = System.currentTimeMillis();
                  ACTIVE.entrySet().removeIf(e -> {
                     UUID id = e.getKey();
                     HealBlockMechanic.Active a = e.getValue();
                     if (a == null) {
                        return SkillResult.SUCCESS;
                     }

                     if (a.expireAtMs <= now) {
                        return SkillResult.SUCCESS;
                     }

                     Entity ent = Bukkit.getEntity(id);
                     if (!(ent instanceof Player)) {
                        return SkillResult.SUCCESS;
                     }

                     Player p = (Player)ent;
                     if (p.isValid() && !p.isDead()) {
                        double maxHp = p.getMaxHealth();
                        if (a.ceiling > maxHp) {
                           a.ceiling = maxHp;
                        }

                        double hp = p.getHealth();
                        if (hp < a.ceiling) {
                           a.ceiling = Math.max(0.0, Math.min(hp, maxHp));
                        }

                        if (hp < a.ceiling) {
                           try {
                              p.setHealth(Math.max(0.0, Math.min(a.ceiling, maxHp)));
                           } catch (Throwable var12) {
                           }
                        }

                        return SkillResult.FAILURE;
                     } else {
                        return SkillResult.SUCCESS;
                     }
                  });
               }, 1L, 1L);
               bootstrapDone = true;
               if (log) {
                  IgaDebugLogger.log("[LifeMoreMythicMobs]", "clamp-only bootstrap (it) started.");
               }
            }
         }
      }
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      Entity bukkit = target.getBukkitEntity();
      if (!(bukkit instanceof Player)) {
         return SkillResult.FAILURE;
      }

      Player player = (Player)bukkit;
      int durationTicks = (int)Math.max(1.0, Math.floor(this.durationTicks.get(data)));
      long expireAt = System.currentTimeMillis() + durationTicks * 50L;
      ACTIVE.compute(player.getUniqueId(), (uuid, existing) -> {
         if (existing != null) {
            if (!this.refresh) {
               return (HealBlockMechanic.Active)existing;
            }

            existing.expireAtMs = expireAt;
            if (this.resetOnRefresh) {
               existing.ceiling = clampCeilingToMax(player, player.getHealth());
            }

            return (HealBlockMechanic.Active)existing;
         } else {
            double ceiling = clampCeilingToMax(player, player.getHealth());
            return new HealBlockMechanic.Active(ceiling, expireAt);
         }
      });
      Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), () -> {
         HealBlockMechanic.Active a = ACTIVE.get(player.getUniqueId());
         if (a != null) {
            if (!player.isDead() && player.isValid()) {
               double maxHp = player.getMaxHealth();
               double hp = player.getHealth();
               if (hp > a.ceiling) {
                  try {
                     player.setHealth(Math.max(0.0, Math.min(a.ceiling, maxHp)));
                  } catch (Throwable var7) {
                  }
               }
            }
         }
      });
      if (this.log) {
         IgaDebugLogger.log(
            this.getClass(),
            String.format(
               "(clamp-only) applied to %s ticks=%d refresh=%s resetOnRefresh=%s ceiling=%.2f",
               player.getName(),
               durationTicks,
               this.refresh,
               this.resetOnRefresh,
               ACTIVE.get(player.getUniqueId()).ceiling
            )
         );
      }

      return SkillResult.SUCCESS;
   }

   private static double clampCeilingToMax(Player p, double candidate) {
      return Math.max(0.0, Math.min(candidate, p.getMaxHealth()));
   }

   private static class Active {
      volatile double ceiling;
      volatile long expireAtMs;

      Active(double ceiling, long expireAtMs) {
         this.ceiling = ceiling;
         this.expireAtMs = expireAtMs;
      }
   }
}
