package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ReticleTittleMechanic extends SkillMechanic implements INoTargetSkill, ITargetedEntitySkill {
   private static final Pattern P_UNICODE = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
   private static final Pattern P_HEX0X = Pattern.compile("\\b0x([0-9a-fA-F]{4})\\b");
   private static final Pattern P_HEX4 = Pattern.compile("\\b([0-9a-fA-F]{4})\\b");
   private static final Map<UUID, BukkitTask> RETICLE_TASKS = new HashMap<>();
   private final PlaceholderString glyph;
   private final PlaceholderDouble duration;
   private final PlaceholderDouble interval;
   private final PlaceholderDouble fadeIn;
   private final PlaceholderDouble fadeOut;
   private final boolean clearAtEnd;
   private final boolean stable;

   public ReticleTittleMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.glyph = PlaceholderString.of(config.getString(new String[]{"glyph", "g", "text"}, "\ue000", new String[0]));
      this.duration = PlaceholderDouble.of(config.getString(new String[]{"duration", "dur", "d"}, "40", new String[0]));
      this.interval = PlaceholderDouble.of(config.getString(new String[]{"interval", "int", "i"}, "2", new String[0]));
      this.fadeIn = PlaceholderDouble.of(config.getString(new String[]{"fadein", "fi"}, "0", new String[0]));
      this.fadeOut = PlaceholderDouble.of(config.getString(new String[]{"fadeout", "fo"}, "0", new String[0]));
      this.clearAtEnd = config.getBoolean(new String[]{"clear", "c"}, true);
      this.stable = config.getBoolean(new String[]{"stable", "sticky"}, true);
      this.setAsyncSafe(false);
   }

   public SkillResult cast(SkillMetadata data) {
      AbstractEntity caster = data.getCaster().getEntity();
      return this.sendToEntity(data, caster);
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      return this.sendToEntity(data, target);
   }

   private boolean sendToEntity(SkillMetadata data, AbstractEntity ae) {
      if (ae != null && !ae.isDead() && ae.isPlayer()) {
         final Player p = (Player)ae.getBukkitEntity();
         if (p != null && p.isOnline()) {
            String glyphRaw = this.glyph.get(data, ae);
            final String glyph = this.safeGlyph(this.decodeUnicodeEscapes(glyphRaw));
            final int duration = Math.max(1, (int)Math.round(this.duration.get(data, ae)));
            final int interval = Math.max(1, (int)Math.round(this.interval.get(data, ae)));
            final int fadeIn = Math.max(0, (int)Math.round(this.fadeIn.get(data, ae)));
            final int fadeOut = Math.max(0, (int)Math.round(this.fadeOut.get(data, ae)));
            final int stay = duration;
            this.stopExisting(p);
            this.clearNow(p);
            if (this.stable) {
               p.sendTitle(glyph, "", fadeIn, stay, fadeOut);
               Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), () -> {
                  if (!p.isOnline()) {
                     this.clearFinally(p);
                  }
               }, duration);
               return SkillResult.SUCCESS;
            } else {
               BukkitTask task = (new BukkitRunnable() {
                  int elapsed = 0;

                  public void run() {
                     if (!p.isOnline()) {
                        this.cancel();
                        ReticleTittleMechanic.RETICLE_TASKS.remove(p.getUniqueId());
                     } else {
                        p.sendTitle(glyph, "", fadeIn, stay, fadeOut);
                        this.elapsed = this.elapsed + interval;
                        if (this.elapsed >= duration) {
                           ReticleTittleMechanic.this.clearFinally(p);
                           this.cancel();
                           ReticleTittleMechanic.RETICLE_TASKS.remove(p.getUniqueId());
                        }
                     }
                  }
               }).runTaskTimer(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), 0L, interval);
               RETICLE_TASKS.put(p.getUniqueId(), task);
               return SkillResult.SUCCESS;
            }
         } else {
            return SkillResult.FAILURE;
         }
      } else {
         return SkillResult.FAILURE;
      }
   }

   private String safeGlyph(String g) {
      String s = Objects.toString(g, "").trim();
      return s.isEmpty() ? " " : s;
   }

   private String decodeUnicodeEscapes(String s) {
      return s != null && !s.isEmpty() ? this.replaceHex(s, P_UNICODE) : "";
   }

   private String replaceHex(String s, Pattern pattern) {
      Matcher m = pattern.matcher(s);
      StringBuffer out = new StringBuffer(s.length());

      while (m.find()) {
         int code = Integer.parseInt(m.group(1), 16);
         String repl = String.valueOf((char)code);
         m.appendReplacement(out, Matcher.quoteReplacement(repl));
      }

      m.appendTail(out);
      return out.toString();
   }

   private void startTask(Player p, long period, final Runnable tick) {
      BukkitTask old = RETICLE_TASKS.remove(p.getUniqueId());
      if (old != null) {
         old.cancel();
      }

      BukkitTask t = (new BukkitRunnable() {
         public void run() {
            tick.run();
         }
      }).runTaskTimer(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), 0L, period);
      RETICLE_TASKS.put(p.getUniqueId(), t);
   }

   private void stopExisting(Player p) {
      BukkitTask t = RETICLE_TASKS.remove(p.getUniqueId());
      if (t != null) {
         t.cancel();
      }
   }

   private void strongClearTitle(Player p) {
      p.resetTitle();
      p.sendTitle("", "", 0, 0, 0);
      Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), () -> p.sendTitle("", "", 0, 0, 0), 1L);
   }

   private void clearNow(Player p) {
      p.resetTitle();
      p.sendTitle("", "", 0, 0, 0);
   }

   private void clearFinally(Player p) {
      p.resetTitle();
      p.sendTitle("", "", 0, 0, 0);
      Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), () -> p.sendTitle("", "", 0, 0, 0), 1L);
   }

   private static final class TaskAbort extends RuntimeException {
      private static final long serialVersionUID = 1L;
   }
}
