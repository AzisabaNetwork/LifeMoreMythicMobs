package net.azisaba.lifemoremythicmobs.util;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class TimerService {
   private final Plugin plugin;
   private final Map<TimerService.TimerKey, TimerService.TimerState> running = new ConcurrentHashMap<>();

   public TimerService(Plugin plugin) {
      this.plugin = plugin;
   }

   public void start(UUID uuid, String purpose, int maxSeconds, Runnable onAutoStop) {
      TimerService.TimerKey key = new TimerService.TimerKey(uuid, purpose);
      this.stopInterval(key);
      TimerService.TimerState st = new TimerService.TimerState(System.currentTimeMillis(), maxSeconds);
      st.autoStopTask = Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
         if (this.running.containsKey(key)) {
            onAutoStop.run();
         }
      }, maxSeconds * 20L);
      this.running.put(key, st);
   }

   public int stopAndGetElapsedSeconds(UUID uuid, String purpose) {
      TimerService.TimerKey key = new TimerService.TimerKey(uuid, purpose);
      TimerService.TimerState st = this.running.remove(key);
      if (st == null) {
         return -1;
      }

      if (st.autoStopTask != null) {
         st.autoStopTask.cancel();
      }

      long now = System.currentTimeMillis();
      long elapsedMs = now - st.startMillis;
      int elapsedSec = (int)Math.floor(elapsedMs / 1000.0);
      if (elapsedSec < 0) {
         elapsedSec = 0;
      }

      if (elapsedSec > st.maxSeconds) {
         elapsedSec = st.maxSeconds;
      }

      return elapsedSec;
   }

   public boolean isRunning(UUID uuid, String purpose) {
      return this.running.containsKey(new TimerService.TimerKey(uuid, purpose));
   }

   private void stopInterval(TimerService.TimerKey key) {
      TimerService.TimerState st = this.running.remove(key);
      if (st != null && st.autoStopTask != null) {
         st.autoStopTask.cancel();
      }
   }

   public void discardAll() {
      for (TimerService.TimerState st : this.running.values()) {
         if (st != null && st.autoStopTask != null) {
            st.autoStopTask.cancel();
         }
      }

      this.running.clear();
   }

   public Integer peekElapsedSeconds(UUID uuid, String purpose) {
      TimerService.TimerState st = this.running.get(new TimerService.TimerKey(uuid, purpose));
      if (st == null) {
         return null;
      }

      long now = System.currentTimeMillis();
      int elapsed = (int)Math.floor((now - st.startMillis) / 1000.0);
      if (elapsed < 0) {
         elapsed = 0;
      }

      if (elapsed > st.maxSeconds) {
         elapsed = st.maxSeconds;
      }

      return elapsed;
   }

   public static final class TimerKey {
      public final UUID uuid;
      public final String purpose;

      public TimerKey(UUID uuid, String purpose) {
         this.uuid = uuid;
         this.purpose = purpose;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         }

         if (!(o instanceof TimerService.TimerKey)) {
            return false;
         }

         TimerService.TimerKey other = (TimerService.TimerKey)o;
         return Objects.equals(this.uuid, other.uuid) && Objects.equals(this.purpose, other.purpose);
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.uuid, this.purpose);
      }
   }

   public static class TimerState {
      public final long startMillis;
      public final int maxSeconds;
      public BukkitTask autoStopTask;

      public TimerState(long startMillis, int maxSeconds) {
         this.startMillis = startMillis;
         this.maxSeconds = maxSeconds;
      }
   }
}
