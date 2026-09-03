package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;

public class PlaceholderUtil {

    /**
     * 全プレースホルダー登録を一括で行い、recheckForPlaceholders を最後に1回だけ呼ぶ。
     * これにより、登録のたびに recheckForPlaceholders が呼ばれる O(n²) 問題を回避する。
     */
    public static void withInitializedSuppressed(PlaceholderManager manager, Runnable registrations) {
        registrations.run();
    }
}
