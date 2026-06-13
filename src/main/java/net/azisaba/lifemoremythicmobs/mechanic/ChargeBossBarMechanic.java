package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ChargeBossBarMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private static final ConcurrentMap<UUID, ConcurrentMap<String, ChargeBossBarMechanic.BossBarHolder>> bossBars = new ConcurrentHashMap<>();
   private final int duration;
   private final PlaceholderDouble current;
   private final double max;
   private final PlaceholderString barTitleRaw;
   private final PlaceholderString barKeyRaw;
   private final BarColor barColor;
   private final BarStyle barStyle;

   public ChargeBossBarMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.duration = config.getInteger("duration", 40);
      this.current = PlaceholderDouble.of(config.getString(new String[]{"current", "charge", "c"}, "0", new String[0]));
      this.max = config.getDouble("max", 100.0);
      this.barTitleRaw = PlaceholderString.of(config.getString("title", "Charge..."));
      this.barKeyRaw = config.getString("barkey") != null ? PlaceholderString.of(config.getString("barkey")) : null;
      this.barColor = this.parseColor(config.getString("color", "BLUE"));
      this.barStyle = this.parseStyle(config.getString("style", "SOLID"));
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      if (!(target.getBukkitEntity() instanceof Player)) {
         return SkillResult.FAILURE;
      } else if (!Bukkit.isPrimaryThread()) {
         Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), () -> this.castAtEntitySync(data, target));
         return SkillResult.SUCCESS;
      } else {
         return this.castAtEntitySync(data, target);
      }
   }

   private boolean castAtEntitySync(SkillMetadata data, AbstractEntity target) {
      Player player = (Player)target.getBukkitEntity();
      UUID uuid = player.getUniqueId();
      String resolvedTitle = ChatColor.translateAlternateColorCodes('&', this.barTitleRaw.get(data));
      String titleKey = ChatColor.stripColor(resolvedTitle).toLowerCase();
      String resolvedBarKey = this.barKeyRaw != null ? this.barKeyRaw.get(data) : null;
      String key = resolvedBarKey != null ? resolvedBarKey : titleKey;
      ConcurrentMap<String, ChargeBossBarMechanic.BossBarHolder> playerBars = bossBars.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
      double current = this.current.get(data);
      double progress = Math.max(0.0, Math.min(1.0, current / this.max));
      if (playerBars.containsKey(key)) {
         ChargeBossBarMechanic.BossBarHolder holder = playerBars.get(key);
         BossBar existing = holder.bossBar;
         if (existing.getColor() == this.barColor && existing.getStyle() == this.barStyle) {
            existing.setTitle(resolvedTitle);
            holder.update(progress, this.duration);
         } else {
            existing.removePlayer(player);
            existing.removeAll();
            holder.cancel();
            BossBar bossBar = Bukkit.createBossBar(resolvedTitle, this.barColor, this.barStyle, new BarFlag[0]);
            bossBar.setProgress(progress);
            bossBar.addPlayer(player);
            ChargeBossBarMechanic.BossBarHolder newHolder = new ChargeBossBarMechanic.BossBarHolder(bossBar, player, this.duration, uuid, key);
            playerBars.put(key, newHolder);
            newHolder.start();
         }
      } else {
         BossBar bossBar = Bukkit.createBossBar(resolvedTitle, this.barColor, this.barStyle, new BarFlag[0]);
         bossBar.setProgress(progress);
         bossBar.addPlayer(player);
         ChargeBossBarMechanic.BossBarHolder holder = new ChargeBossBarMechanic.BossBarHolder(bossBar, player, this.duration, uuid, key);
         playerBars.put(key, holder);
         holder.start();
      }

      return SkillResult.SUCCESS;
   }

   public BarColor parseColor(String name) {
      try {
         return BarColor.valueOf(name.toUpperCase());
      } catch (IllegalArgumentException e) {
         return BarColor.BLUE;
      }
   }

   private BarStyle parseStyle(String name) {
      try {
         return BarStyle.valueOf(name.toUpperCase());
      } catch (IllegalArgumentException e) {
         return BarStyle.SOLID;
      }
   }

   private static class BossBarHolder {
      private final BossBar bossBar;
      private final Player player;
      private final UUID uuid;
      private final String key;
      private int remainingTicks;
      private BukkitRunnable task;

      BossBarHolder(BossBar bossBar, Player player, int duration, UUID uuid, String key) {
         this.bossBar = bossBar;
         this.player = player;
         this.uuid = uuid;
         this.key = key;
         this.remainingTicks = duration;
      }

      void update(double progress, int duration) {
         this.bossBar.setProgress(progress);
         this.remainingTicks = duration;
      }

      void start() {
         this.task = new BukkitRunnable() {
            public void run() {
               try {
                  BossBarHolder.this.remainingTicks = BossBarHolder.this.remainingTicks - 1;
                  if (BossBarHolder.this.remainingTicks <= 0 || !BossBarHolder.this.player.isOnline()) {
                     BossBarHolder.this.cleanUpAndCancel();
                  }
               } catch (Throwable t) {
                  BossBarHolder.this.cleanUpAndCancel();
               }
            }
         };
         this.task.runTaskTimer(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), 0L, 1L);
      }

      void cancel() {
         if (this.task != null) {
            this.task.cancel();
         }
      }

      private void cleanUpAndCancel() {
         try {
            this.bossBar.removePlayer(this.player);
            this.bossBar.removeAll();
         } finally {
            ConcurrentMap<String, ChargeBossBarMechanic.BossBarHolder> playerMap = ChargeBossBarMechanic.bossBars.get(this.uuid);
            if (playerMap != null) {
               playerMap.remove(this.key);
               if (playerMap.isEmpty()) {
                  ChargeBossBarMechanic.bossBars.remove(this.uuid);
               }
            }

            this.cancel();
         }
      }
   }
}
