package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.core.skills.triggers.meta.EntityAttackMetadata;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * Critical hits on melee attacks, built from several sources and rolled once per hit.
 *
 * <p>Each {@code critChance} line run by an {@code ~onAttack} skill adds its chance to the hit, and
 * {@code critChanceBuff} adds a timed chance to the attacker. All chances are summed and rolled once,
 * so four 5% items give one 20% roll, not four separate ones. The roll happens in the
 * {@link EntityAttackMetadata} finalizer, which MythicMobs runs when it writes the final damage back
 * to the event, after every attack skill has run.</p>
 */
public final class CritRoll implements Function<Double, Double> {
    private static final Map<UUID, Map<String, Buff>> BUFFS = new ConcurrentHashMap<>();

    private final Function<Double, Double> previous;
    private final UUID attacker;
    private double chance;
    private double multiplier = 1.0D;
    private Boolean critical;

    private CritRoll(Function<Double, Double> previous, UUID attacker) {
        this.previous = previous;
        this.attacker = attacker;
    }

    /** The roll of this hit, installed on first use. */
    public static CritRoll of(EntityAttackMetadata meta, UUID attacker) {
        if (meta.getFinalizer() instanceof CritRoll roll) {
            return roll;
        }
        CritRoll roll = new CritRoll(meta.getFinalizer(), attacker);
        meta.setFinalizer(roll);
        return roll;
    }

    /** Adds a chance (0.05 = 5%) with the damage multiplier used when it crits (the highest one wins). */
    public void add(double chance, double multiplier) {
        if (critical != null) {
            return; // already rolled
        }
        this.chance += chance;
        this.multiplier = Math.max(this.multiplier, multiplier);
    }

    @Override
    public Double apply(Double damage) {
        double value = previous == null ? damage : previous.apply(damage);
        if (critical == null) {
            double total = chance;
            double mult = multiplier;
            for (Buff buff : activeBuffs(attacker)) {
                total += buff.chance;
                mult = Math.max(mult, buff.multiplier);
            }
            critical = total > 0.0D && ThreadLocalRandom.current().nextDouble() < total;
            multiplier = mult;
        }
        return critical ? value * multiplier : value;
    }

    public static void addBuff(UUID entity, String id, double chance, double multiplier, long durationMs) {
        BUFFS.computeIfAbsent(entity, k -> new ConcurrentHashMap<>())
                .put(id, new Buff(chance, multiplier, System.currentTimeMillis() + durationMs));
    }

    public static boolean hasBuff(UUID entity) {
        return !activeBuffs(entity).isEmpty();
    }

    private static java.util.List<Buff> activeBuffs(UUID entity) {
        Map<String, Buff> map = BUFFS.get(entity);
        if (map == null) {
            return java.util.List.of();
        }
        long now = System.currentTimeMillis();
        map.values().removeIf(b -> b.expireAt <= now);
        if (map.isEmpty()) {
            BUFFS.remove(entity, map);
            return java.util.List.of();
        }
        return java.util.List.copyOf(map.values());
    }

    private record Buff(double chance, double multiplier, long expireAt) {}
}
