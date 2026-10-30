package net.azisaba.lifemoremythicmobs.listener;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicTriggerEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.skills.SkillTriggers;
import io.lumine.mythic.core.skills.TriggeredSkill;
import io.lumine.mythic.core.skills.triggers.meta.DamagedMetadata;
import io.lumine.mythic.core.skills.triggers.meta.EntityAttackMetadata;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Restores how MythicMobs 4.12 treated damage a mob takes, where 5.12 changed it.
 * <ul>
 *   <li>{@code ~onDamaged} only fired for damage dealt by an entity. 5.12 also fires it for fall,
 *       fire, suffocation and other environment damage, so invulnerability phases built on
 *       {@code CancelEvent} blocked that damage and other skills went off on every fall.</li>
 *   <li>A hit cancelled by the mob's own {@code ~onDamaged} skills still added threat. 5.12 stops
 *       before the threat table, so a boss that is invulnerable for a phase gathers no threat.</li>
 *   <li>A {@code DamageModifiers} value of 0 for an environment cause (e.g. {@code SUFFOCATION 0})
 *       zeroed the damage. 5.12 only does that with {@code Mobs.CancelDamageIfZero}.</li>
 * </ul>
 * Each applies only to mobs that opt in with {@code LegacyDamage} (see {@link LegacyDamage}).
 */
public final class LegacyMobDamageListener implements Listener {
    /** Damaged triggers of hits in progress, to see after MythicMobs whether its skills cancelled the hit. */
    private final Map<EntityDamageByEntityEvent, Hit> hits = new IdentityHashMap<>();

    @EventHandler
    public void onTrigger(MythicTriggerEvent event) {
        if (event.getTrigger() != SkillTriggers.DAMAGED || !(event.getSkillMetadata().getCaster() instanceof ActiveMob mob)) {
            return;
        }
        if (event.getTriggerMetadata() instanceof DamagedMetadata) {
            // environment damage: no ~onDamaged in 4.12
            if (LegacyDamage.enabled(mob, LegacyDamage.Part.ENVIRONMENT_TRIGGER)) event.setCancelled(true);
        } else if (event.getTriggerMetadata() instanceof EntityAttackMetadata attack && attack.getEvent() != null
                && LegacyDamage.enabled(mob, LegacyDamage.Part.CANCELLED_THREAT)
                && event.getParent() instanceof TriggeredSkill skill && !event.isAsynchronous()) {
            hits.put(attack.getEvent(), new Hit(mob, event.getSkillMetadata().getTrigger(), skill));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHit(EntityDamageByEntityEvent event) {
        Hit hit = hits.remove(event);
        if (hit == null || !event.isCancelled() || !hit.skill().getCancelled() || hit.damager() == null) {
            return;
        }
        ActiveMob mob = hit.mob();
        if (mob.getType().usesThreatTable() && mob.getType().getThreatTableUseDamageTaken()
                && mob.getThreatTable() != null && !mob.getUniqueId().equals(hit.damager().getUniqueId())) {
            mob.getThreatTable().threatGain(hit.damager(), event.getDamage());
        }
    }

    // After MythicMobs' own handler at the same priority (it is registered first)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnvironmentDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) return; // the combat path already applies 0
        ActiveMob mob = MythicBukkit.inst().getMobManager().getMythicMobInstance(event.getEntity());
        if (mob == null || !LegacyDamage.enabled(mob, LegacyDamage.Part.ZERO_MODIFIER)) return;
        Map<String, Double> modifiers = mob.getType().getDamageModifiers();
        if (modifiers == null) return;
        Double modifier = modifiers.get(event.getCause().toString().toUpperCase(Locale.ROOT));
        if (modifier != null && modifier == 0.0) {
            event.setDamage(0.0);
        }
    }

    private record Hit(ActiveMob mob, AbstractEntity damager, TriggeredSkill skill) {
    }
}
