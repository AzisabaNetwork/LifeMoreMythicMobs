package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.bukkit.BukkitAdapter;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages active parry/counter windows on entities.
 */
public final class ParryWindowManager {
    private static final Map<UUID, WindowEntry> WINDOWS = new ConcurrentHashMap<>();

    private ParryWindowManager() {
    }

    public static final class WindowEntry {
        public final SkillMetadata data;
        public final double reduceRatio;
        public final boolean consume;
        public final String onParrySkill;
        public final String onExpireSkill;
        public final BukkitTask expireTask;

        WindowEntry(SkillMetadata data, double reduceRatio, boolean consume,
                    String onParrySkill, String onExpireSkill, BukkitTask expireTask) {
            this.data = data;
            this.reduceRatio = Math.max(0.0, Math.min(1.0, reduceRatio));
            this.consume = consume;
            this.onParrySkill = onParrySkill;
            this.onExpireSkill = onExpireSkill;
            this.expireTask = expireTask;
        }
    }

    public static void activate(UUID targetId, int durationTicks, double reduceRatio, boolean consume,
                                String onParrySkill, String onExpireSkill, SkillMetadata data) {
        cancel(targetId);
        SkillMetadata cloned = data.deepClone();
        BukkitTask task = Bukkit.getScheduler().runTaskLater(LifeMoreMythicMobs.inst(), () -> {
            WindowEntry removed = WINDOWS.remove(targetId);
            if (removed != null && removed.onExpireSkill != null && !removed.onExpireSkill.isEmpty()) {
                SkillUtil.executeSkill(removed.onExpireSkill, removed.data, SkillUtil.getAbstractEntity(targetId));
            }
        }, Math.max(1, durationTicks));

        WINDOWS.put(targetId, new WindowEntry(cloned, reduceRatio, consume, onParrySkill, onExpireSkill, task));
    }

    public static boolean isActive(UUID targetId) {
        return WINDOWS.containsKey(targetId);
    }

    /**
     * Evaluates incoming damage against an active parry window.
     *
     * @return remaining damage multiplier (e.g. 0.0 for full parry), or -1 if no parry window is active.
     */
    public static double tryParry(UUID defenderId, Entity attacker) {
        WindowEntry entry = WINDOWS.get(defenderId);
        if (entry == null) return -1.0;

        if (entry.consume) {
            WINDOWS.remove(defenderId, entry);
            if (entry.expireTask != null) entry.expireTask.cancel();
        }

        if (entry.onParrySkill != null && !entry.onParrySkill.isEmpty()) {
            SkillUtil.executeSkill(entry.onParrySkill, entry.data, attacker != null ? BukkitAdapter.adapt(attacker) : null);
        }
        return 1.0 - entry.reduceRatio;
    }

    public static void cancel(UUID targetId) {
        WindowEntry old = WINDOWS.remove(targetId);
        if (old != null && old.expireTask != null) {
            old.expireTask.cancel();
        }
    }

    public static void clearAll() {
        for (WindowEntry e : WINDOWS.values()) {
            if (e.expireTask != null) e.expireTask.cancel();
        }
        WINDOWS.clear();
    }
}
