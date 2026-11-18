package net.azisaba.lifemoremythicmobs.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一定時間だけ足す属性防御値・属性防御強度値（elemDefBuff で付ける）。
 * HoroloCore の装飾品・宝玉の値に上乗せして、mobTDamage の属性防御の計算に使う。
 * 同じ名前で付け直すと上書き（時間も値も新しいものになる）。違う名前なら重なる。
 */
public final class TempElementalDefense {
    private static final Map<UUID, Map<String, Buff>> BUFFS = new ConcurrentHashMap<>();

    private TempElementalDefense() {
    }

    /** elements が null か空なら全属性 */
    public static void put(UUID player, String name, Set<String> elements, double armor, double toughness, long ticks) {
        if (player == null || name == null || ticks <= 0) return;
        long until = System.currentTimeMillis() + ticks * 50L;
        Set<String> el = elements == null || elements.isEmpty() ? null : Collections.unmodifiableSet(new HashSet<>(elements));
        BUFFS.computeIfAbsent(player, k -> new ConcurrentHashMap<>()).put(name.toLowerCase(Locale.ROOT),
                new Buff(el, armor, toughness, until));
    }

    public static void remove(UUID player, String name) {
        if (player == null || name == null) return;
        Map<String, Buff> m = BUFFS.get(player);
        if (m != null) m.remove(name.toLowerCase(Locale.ROOT));
    }

    public static boolean has(UUID player, String name) {
        Map<String, Buff> m = player == null ? null : BUFFS.get(player);
        if (m == null || name == null) return false;
        Buff b = m.get(name.toLowerCase(Locale.ROOT));
        return b != null && b.until > System.currentTimeMillis();
    }

    /** {armor, toughness}。切れたものは捨てる */
    public static double[] sum(UUID player, String element) {
        Map<String, Buff> m = player == null ? null : BUFFS.get(player);
        if (m == null || m.isEmpty()) return new double[] {0.0D, 0.0D};
        long now = System.currentTimeMillis();
        String el = element == null ? null : element.trim().toUpperCase(Locale.ROOT);
        double a = 0.0D, t = 0.0D;
        for (Map.Entry<String, Buff> e : m.entrySet()) {
            Buff b = e.getValue();
            if (b.until <= now) {
                m.remove(e.getKey());
                continue;
            }
            if (b.elements != null && (el == null || !b.elements.contains(el))) continue;
            a += b.armor;
            t += b.toughness;
        }
        return new double[] {a, t};
    }

    private record Buff(Set<String> elements, double armor, double toughness, long until) {
    }
}
