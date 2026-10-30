package net.azisaba.lifemoremythicmobs.listener;

import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Restores the MythicMobs 4.12 handling of skill damage a mob deals to itself.
 *
 * <p>In 4.12 such a hit (e.g. {@code damagepercent @self}) never ran the mob's own
 * {@code ~onDamaged} skills: "using a damage skill" was a plain flag. 5.12 keeps the
 * "Mob Hit Self" check, but the flag became a counter that only reads true at 3, while a
 * single damage call raises it to 1. The mob's own {@code ~onDamaged} therefore runs, and an
 * invulnerability phase built on {@code CancelEvent} cancels the mob's own damage (e.g. a boss
 * that should lose HP whenever a minion dies). Holding the counter at 3 while MythicMobs
 * applies a self-targeted hit makes the check work again.</p>
 *
 * <p>Applies only to mobs that opt in with {@code LegacyDamage} (see {@link LegacyDamage}).</p>
 */
public final class LegacySelfDamageListener implements Listener {
    private static final int USING_DAMAGE_SKILL = 3;

    private final Field counter;
    private final Map<MythicDamageEvent, Integer> saved = new IdentityHashMap<>();

    public LegacySelfDamageListener(Plugin plugin) {
        Field field = null;
        try {
            field = ActiveMob.class.getDeclaredField("usingDamageSkill");
            field.setAccessible(true);
        } catch (ReflectiveOperationException | RuntimeException e) {
            plugin.getLogger().log(Level.WARNING, "ActiveMob.usingDamageSkill not found; self-damage fix disabled", e);
        }
        this.counter = field;
    }

    // MythicMobs applies the hit at HIGH; the Bukkit damage event it causes is handled inside that call
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void beforeMythicDamage(MythicDamageEvent event) {
        if (counter == null) return;
        SkillCaster caster = event.getDamageMetadata().getDamager();
        if (!(caster instanceof ActiveMob mob) || caster.getEntity() == null
                || !caster.getEntity().getUniqueId().equals(event.getTarget().getUniqueId())
                || !LegacyDamage.enabled(mob, LegacyDamage.Part.SELF_DAMAGE)) {
            return;
        }
        try {
            saved.put(event, counter.getInt(mob));
            counter.setInt(mob, USING_DAMAGE_SKILL);
        } catch (IllegalAccessException ignored) {
            saved.remove(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterMythicDamage(MythicDamageEvent event) {
        Integer previous = saved.remove(event);
        if (previous == null) return;
        try {
            counter.setInt(event.getDamageMetadata().getDamager(), previous);
        } catch (IllegalAccessException ignored) {
            // counter already accessible; cannot happen
        }
    }
}
