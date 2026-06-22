package net.azisaba.lifemoremythicmobs.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalCooldownManager {

    // key: gcdname -> expiry tick (System.currentTimeMillis() based)
    private static final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    /**
     * 指定したgcdnameがクールタイム中かどうかを確認する。
     */
    public static boolean isOnCooldown(String gcdName) {
        Long expiry = cooldowns.get(gcdName);
        if (expiry == null) return false;
        if (System.currentTimeMillis() < expiry) return true;
        cooldowns.remove(gcdName);
        return false;
    }

    /**
     * 指定したgcdnameにクールタイムをセットする。
     * @param gcdName 管理名
     * @param ticks クールタイム（tick単位、1tick = 50ms）
     */
    public static void setCooldown(String gcdName, int ticks) {
        long expiryMs = System.currentTimeMillis() + (long) ticks * 50L;
        cooldowns.put(gcdName, expiryMs);
    }

    /**
     * 指定したgcdnameのクールタイムを削除する。
     */
    public static void removeCooldown(String gcdName) {
        cooldowns.remove(gcdName);
    }
}
