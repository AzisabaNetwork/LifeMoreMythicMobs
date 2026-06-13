package net.azisaba.lifemoremythicmobs.placeholders;

import net.azisaba.lifemoremythicmobs.util.TimerRepository;
import net.azisaba.lifemoremythicmobs.util.TimerService;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.placeholders.Placeholder;
import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TimerElapsedPlaceholder {
   private static final ConcurrentHashMap<String, TimerElapsedPlaceholder.CacheEntry> CACHE = new ConcurrentHashMap<>();
   private static final long CACHE_MS = 2000L;

   public static void register(PlaceholderManager manager, TimerService timerService, TimerRepository repo) {
      manager.register("timer_elapsed", Placeholder.meta((meta, arg) -> {
         try {
            if (!(meta instanceof SkillMetadata)) {
               return "0";
            }

            SkillMetadata data = (SkillMetadata)meta;
            UUID uuid = data.getCaster() != null && data.getCaster().getEntity() != null ? data.getCaster().getEntity().getUniqueId() : null;
            if (uuid == null) {
               return "0";
            }

            String purpose = arg != null && !arg.trim().isEmpty() ? arg.trim() : "default";
            Integer runningElapsed = timerService.peekElapsedSeconds(uuid, purpose);
            if (runningElapsed != null) {
               return String.valueOf(runningElapsed);
            }

            long now = System.currentTimeMillis();
            String cacheKey = uuid.toString() + "|" + purpose;
            TimerElapsedPlaceholder.CacheEntry ce = CACHE.get(uuid);
            if (ce != null && now - ce.at <= 2000L) {
               return String.valueOf(ce.value);
            }

            int dbValue = repo.getElapsedSec(uuid, purpose);
            CACHE.put(cacheKey, new TimerElapsedPlaceholder.CacheEntry(dbValue, now));
            return String.valueOf(dbValue);
         } catch (Exception e) {
            return "0";
         }
      }));
   }

   private static class CacheEntry {
      final int value;
      final long at;

      CacheEntry(int value, long at) {
         this.value = value;
         this.at = at;
      }
   }
}
