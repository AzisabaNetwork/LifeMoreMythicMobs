package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.core.skills.SkillExecutor;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Makes MythicMobs' own {@code damage} (alias {@code d}) run as LifeMore's tDamage.
 * <p>
 * MythicMobs resolves built-in mechanics from a static name table filled once at startup, and only
 * asks other plugins (MythicMechanicLoadEvent) for names that are not in it. Removing "DAMAGE" and
 * "D" from that table hands {@code damage{...}} to {@link net.azisaba.lifemoremythicmobs.listener.Register},
 * which builds a TypedDamageMechanic. tDamage has every option of damage (same DamagingMechanic base);
 * it adds element buffs, upgrades, several elements and reads {@code type=}/{@code t=} as the element.
 */
public final class DamageMechanicOverride {
    private static volatile boolean active;

    private DamageMechanicOverride() {}

    public static boolean isActive() {
        return active;
    }

    @SuppressWarnings("unchecked")
    public static void apply(Logger logger) {
        try {
            Field field = SkillExecutor.class.getDeclaredField("MECHANICS");
            field.setAccessible(true);
            Map<String, Class<?>> mechanics = (Map<String, Class<?>>) field.get(null);
            int removed = 0;
            for (String name : new String[]{"DAMAGE", "D"}) {
                Class<?> clazz = mechanics.get(name);
                if (clazz != null && clazz.getSimpleName().equals("DamageMechanic")) {
                    mechanics.remove(name);
                    removed++;
                }
            }
            active = true;
            logger.info("MythicMobs の damage メカニクスを tDamage に差し替えました (" + removed + " 件)。"
                    + "起動直後に MythicMobs を1回リロードして反映します。");
        } catch (ReflectiveOperationException | RuntimeException e) {
            active = false;
            logger.warning("damage メカニクスを tDamage に差し替えられませんでした: " + e);
        }
    }
}
