package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class HudTextMechanic extends SkillMechanic implements INoTargetSkill, ITargetedEntitySkill {
   private final PlaceholderString msg;
   private final PlaceholderDouble offsetPx;
   private final int totalPx;
   private final int durationTicks;
   private final int intervalTicks;
   private final char spaceChar;
   private static final Map<UUID, BukkitTask> RUNNING = new ConcurrentHashMap<>();

   public HudTextMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.msg = PlaceholderString.of(config.getString(new String[]{"msg"}, "HELLO", new String[0]));
      this.offsetPx = PlaceholderDouble.of(config.getString(new String[]{"offset"}, "0", new String[0]));
      this.totalPx = Math.max(0, config.getInteger(new String[]{"total"}, 256));
      this.durationTicks = config.getInteger(new String[]{"duration"}, 200);
      this.intervalTicks = Math.max(1, config.getInteger(new String[]{"interval"}, 10));
      String hex = config.getString(new String[]{"spacehex"}, "E200", new String[0]).replace("\\u", "").replace("0x", "").trim();
      this.spaceChar = (char)Integer.parseInt(hex, 16);
   }

   public SkillResult cast(SkillMetadata data) {
      AbstractEntity ae = data.getCaster().getEntity();
      return this.castAtEntity(data, ae);
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      if (target == null) {
         return SkillResult.FAILURE;
      }

      Entity e = BukkitAdapter.adapt(target);
      if (!(e instanceof Player)) {
         return SkillResult.FAILURE;
      }

      final Player p = (Player)e;
      String text = this.msg.get(data, target);
      if (text == null) {
         text = "";
      }

      int off = (int)Math.round(this.offsetPx.get(data));
      int tot = Math.max(0, this.totalPx);
      int leftPx = Math.max(0, tot / 2 + off);
      int rightPx = Math.max(0, tot - leftPx);
      int l8 = leftPx / 8;
      int r8 = rightPx / 8;
      StringBuilder sb = new StringBuilder(l8 + text.length() + r8);

      for (int i = 0; i < l8; i++) {
         sb.append(this.spaceChar);
      }

      sb.append(text);

      for (int i = 0; i < r8; i++) {
         sb.append(this.spaceChar);
      }

      String cps = text.chars().mapToObj(c -> String.format("U+%04x", c)).reduce((a, b) -> a + " " + b).orElse("");
      IgaDebugLogger.log(
         this.getClass(), String.format("totalPx=%d, offsetPx=%d -> leftPx=%d, rightPx=%d, l8=%d, r8=%d, textCP=%s", tot, off, leftPx, rightPx, l8, r8, cps)
      );
      final String line = sb.toString();
      stopTask(p.getUniqueId(), true);
      BukkitTask task = (new BukkitRunnable() {
         int passed = 0;

         public void run() {
            if (!p.isOnline()) {
               HudTextMechanic.stopTask(p.getUniqueId(), false);
               this.cancel();
            } else {
               p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(line));
               this.passed = this.passed + HudTextMechanic.this.intervalTicks;
               if (this.passed >= HudTextMechanic.this.durationTicks) {
                  p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                  HudTextMechanic.stopTask(p.getUniqueId(), false);
                  this.cancel();
               }
            }
         }
      }).runTaskTimer(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), 0L, this.intervalTicks);
      RUNNING.put(p.getUniqueId(), task);
      return SkillResult.SUCCESS;
   }

   private static void stopTask(UUID uuid, boolean clearNow) {
      BukkitTask t = RUNNING.remove(uuid);
      if (t != null) {
         t.cancel();
      }

      if (clearNow) {
         Player p = Bukkit.getPlayer(uuid);
         if (p != null && p.isOnline()) {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
         }
      }
   }
}
