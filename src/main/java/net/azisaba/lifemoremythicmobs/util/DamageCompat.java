package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.core.skills.damage.DamagingMechanic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Calls {@code DamagingMechanic#doDamage(SkillMetadata, AbstractEntity, double)} regardless of its return type.
 * MythicMobs 5.12 declares it {@code void}, 5.14 declares it {@code boolean}. A direct call compiled against one
 * version fails with NoSuchMethodError on the other, so look it up by name and parameters at runtime.
 */
public final class DamageCompat {
    private static volatile Method method;

    private DamageCompat() {
    }

    public static void doDamage(DamagingMechanic mechanic, SkillMetadata data, AbstractEntity target, double amount) {
        Method m = method;
        if (m == null) {
            try {
                m = DamagingMechanic.class.getDeclaredMethod("doDamage", SkillMetadata.class, AbstractEntity.class, double.class);
                m.setAccessible(true);
                method = m;
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("DamagingMechanic#doDamage(SkillMetadata, AbstractEntity, double) not found", e);
            }
        }
        try {
            m.invoke(mechanic, data, target, amount);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) throw re;
            if (cause instanceof Error er) throw er;
            throw new IllegalStateException(cause);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
