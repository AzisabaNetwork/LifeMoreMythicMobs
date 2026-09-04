package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;

import java.lang.reflect.Field;

public class PlaceholderUtil {

    /**
     * 全プレースホルダー登録を一括で行い、recheckForPlaceholders を最後に1回だけ呼ぶ。
     * これにより、登録のたびに recheckForPlaceholders が呼ばれる O(n²) 問題を回避する。
     */
    public static void withInitializedSuppressed(PlaceholderManager manager, Runnable registrations) {
        synchronized (manager) {
            Field initialized = findField(manager.getClass(), "initialized");
            try {
                boolean wasInitialized = initialized.getBoolean(manager);
                initialized.setBoolean(manager, false);
                try {
                    registrations.run();
                } finally {
                    initialized.setBoolean(manager, wasInitialized);
                }
                if (wasInitialized) {
                    manager.recheckForPlaceholders();
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unable to batch MythicMobs placeholder registration", e);
            }
        }
    }

    private static Field findField(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                // Continue with the superclass. MythicMobs may expose the implementation through a subclass.
            }
        }
        throw new IllegalStateException("MythicMobs PlaceholderManager has no initialized state");
    }
}
