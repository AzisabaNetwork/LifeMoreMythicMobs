package net.azisaba.lifemoremythicmobs.util;

import io.lumine.xikage.mythicmobs.skills.placeholders.PlaceholderManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class PlaceholderUtil {

    private static final Logger LOGGER = Logger.getLogger("LifeMoreMythicMobs");

    /**
     * 全プレースホルダー登録を一括で行い、recheckForPlaceholders を最後に1回だけ呼ぶ。
     * これにより、登録のたびに recheckForPlaceholders が呼ばれる O(n²) 問題を回避する。
     */
    public static void withInitializedSuppressed(PlaceholderManager manager, Runnable registrations) {
        Field initializedField = null;
        boolean wasInitialized = false;
        try {
            initializedField = PlaceholderManager.class.getDeclaredField("initialized");
            initializedField.setAccessible(true);
            wasInitialized = initializedField.getBoolean(manager);
            if (wasInitialized) {
                initializedField.setBoolean(manager, false);
            }
        } catch (Exception e) {
            LOGGER.warning("[LifeMoreMythicMobs] PlaceholderUtil: initialized フィールドへのアクセスに失敗しました: " + e.getMessage());
            initializedField = null;
        }

        try {
            registrations.run();
        } finally {
            if (initializedField != null && wasInitialized) {
                try {
                    initializedField.setBoolean(manager, true);
                    Method recheck = PlaceholderManager.class.getDeclaredMethod("recheckForPlaceholders");
                    recheck.setAccessible(true);
                    recheck.invoke(manager);
                } catch (Exception e) {
                    LOGGER.warning("[LifeMoreMythicMobs] PlaceholderUtil: recheckForPlaceholders の呼び出しに失敗しました: " + e.getMessage());
                }
            }
        }
    }
}
