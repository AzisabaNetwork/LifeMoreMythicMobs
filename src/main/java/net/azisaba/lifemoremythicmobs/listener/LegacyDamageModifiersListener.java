package net.azisaba.lifemoremythicmobs.listener;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Restores the MythicMobs 4.12 DamageModifiers selection semantics.
 *
 * <p>In 4.12, elemental skill damage selected exactly one modifier: the
 * element when present, otherwise SKILL. 5.12 applies SKILL first and then
 * applies the element again as a damage tag. Temporarily hiding SKILL while
 * an elemental MythicDamageEvent is processed prevents that double match
 * without requiring any changes to existing mob or skill YAML.</p>
 */
public final class LegacyDamageModifiersListener implements Listener {
    private final Map<MythicDamageEvent, LegacyDamageModifierScope> scopes = new IdentityHashMap<>();

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void beforeMythicDamage(MythicDamageEvent event) {
        ActiveMob target = MythicBukkit.inst().getMobManager().getMythicMobInstance(event.getTarget());
        if (target == null) {
            return;
        }

        Map<String, Double> modifiers = target.getType().getDamageModifiers();
        if (modifiers == null) {
            return;
        }

        String element = event.getDamageMetadata().getElement();
        LegacyDamageModifierScope scope = LegacyDamageModifierScope.enter(
                modifiers,
                element != null && !element.isBlank()
        );
        scopes.put(event, scope);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterMythicDamage(MythicDamageEvent event) {
        LegacyDamageModifierScope scope = scopes.remove(event);
        if (scope != null) {
            scope.close();
        }
    }
}
