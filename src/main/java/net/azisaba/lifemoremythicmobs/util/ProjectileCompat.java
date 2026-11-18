package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.core.skills.projectiles.Projectile;

import java.lang.reflect.Field;

/**
 * Reads {@code ticksPerSecond} across MythicMobs versions.
 * 5.12 keeps it on {@link Projectile} (the mechanic); 5.14 moved it to {@code Projectile.ProjectileTracker}.
 * A direct field access compiled against one version fails with NoSuchFieldError on the other.
 */
public final class ProjectileCompat {
    private static volatile Field trackerField;
    private static volatile Field mechanicField;
    private static volatile boolean resolved;

    private ProjectileCompat() {
    }

    private static void resolve() {
        if (resolved) return;
        synchronized (ProjectileCompat.class) {
            if (resolved) return;
            trackerField = find(Projectile.ProjectileTracker.class, "ticksPerSecond");
            mechanicField = find(Projectile.class, "ticksPerSecond");
            resolved = true;
        }
    }

    private static Field find(Class<?> owner, String name) {
        try {
            Field f = owner.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    /** @return ticks per second of this projectile (20 / interval); 20 if it cannot be read. */
    public static float ticksPerSecond(Projectile mechanic, Projectile.ProjectileTracker tracker) {
        resolve();
        try {
            if (trackerField != null && tracker != null) {
                float v = trackerField.getFloat(tracker);
                if (v > 0) return v;
            }
            if (mechanicField != null && mechanic != null) {
                float v = mechanicField.getFloat(mechanic);
                if (v > 0) return v;
            }
        } catch (IllegalAccessException ignored) {
        }
        return 20.0F;
    }
}
