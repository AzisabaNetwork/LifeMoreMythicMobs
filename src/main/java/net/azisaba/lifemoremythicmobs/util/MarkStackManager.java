package net.azisaba.lifemoremythicmobs.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks expiring named mark stacks per entity (e.g. resonance stacks, boss debuffs, combo marks).
 */
public final class MarkStackManager {
    private static final Map<UUID, Map<String, MarkEntry>> STORE = new ConcurrentHashMap<>();

    private MarkStackManager() {
    }

    public static final class MarkEntry {
        public int stacks;
        public long expiresAtMs;

        MarkEntry(int stacks, long expiresAtMs) {
            this.stacks = stacks;
            this.expiresAtMs = expiresAtMs;
        }
    }

    public static final class AddResult {
        public final int newStacks;
        public final boolean reachedMax;

        AddResult(int newStacks, boolean reachedMax) {
            this.newStacks = newStacks;
            this.reachedMax = reachedMax;
        }
    }

    public static int getStacks(UUID entityId, String markId) {
        return getStacks(entityId, markId, System.currentTimeMillis());
    }

    public static int getStacks(UUID entityId, String markId, long nowMs) {
        if (entityId == null || markId == null) return 0;
        Map<String, MarkEntry> map = STORE.get(entityId);
        if (map == null) return 0;
        String key = markId.toLowerCase();
        MarkEntry entry = map.get(key);
        if (entry == null) return 0;
        if (entry.expiresAtMs > 0 && nowMs > entry.expiresAtMs) {
            map.remove(key, entry);
            return 0;
        }
        return entry.stacks;
    }

    public static AddResult addStacks(UUID entityId, String markId, int delta, int maxStacks,
                                      long durationMs, boolean resetOnMax, long nowMs) {
        if (entityId == null || markId == null) return new AddResult(0, false);
        Map<String, MarkEntry> map = STORE.computeIfAbsent(entityId, k -> new ConcurrentHashMap<>());
        String key = markId.toLowerCase();
        int current = getStacks(entityId, key, nowMs);
        int next = Math.max(0, current + delta);
        boolean reachedMax = maxStacks > 0 && next >= maxStacks;
        if (maxStacks > 0) {
            next = Math.min(next, maxStacks);
        }
        if (reachedMax && resetOnMax) {
            map.remove(key);
            return new AddResult(0, true);
        }
        if (next <= 0) {
            map.remove(key);
            return new AddResult(0, false);
        }
        long exp = durationMs > 0 ? nowMs + durationMs : -1L;
        map.put(key, new MarkEntry(next, exp));
        return new AddResult(next, reachedMax);
    }

    public static void setStacks(UUID entityId, String markId, int value, long durationMs, long nowMs) {
        if (entityId == null || markId == null) return;
        Map<String, MarkEntry> map = STORE.computeIfAbsent(entityId, k -> new ConcurrentHashMap<>());
        String key = markId.toLowerCase();
        if (value <= 0) {
            map.remove(key);
            return;
        }
        long exp = durationMs > 0 ? nowMs + durationMs : -1L;
        map.put(key, new MarkEntry(value, exp));
    }

    public static int detonate(UUID entityId, String markId, long nowMs) {
        int current = getStacks(entityId, markId, nowMs);
        clear(entityId, markId);
        return current;
    }

    public static void clear(UUID entityId, String markId) {
        if (entityId == null || markId == null) return;
        Map<String, MarkEntry> map = STORE.get(entityId);
        if (map != null) {
            map.remove(markId.toLowerCase());
        }
    }

    public static void removeAll(UUID entityId) {
        if (entityId != null) {
            STORE.remove(entityId);
        }
    }

    public static void clearAll() {
        STORE.clear();
    }
}
