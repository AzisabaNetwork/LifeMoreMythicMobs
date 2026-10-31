package net.azisaba.lifemoremythicmobs.listener;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.skills.damage.DamageMetadata;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import io.lumine.mythic.bukkit.events.MythicStatChangeEvent;
import io.lumine.mythic.bukkit.events.MythicTriggerEvent;
import io.lumine.mythic.core.skills.stats.Stats;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.skills.SkillTriggers;
import io.lumine.mythic.core.skills.TriggeredSkill;
import io.lumine.mythic.core.skills.triggers.meta.DamagedMetadata;
import io.lumine.mythic.core.skills.triggers.meta.EntityAttackMetadata;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import org.bukkit.metadata.MetadataValue;

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
 *   <li>The mob option {@code Armor} was taken off every hit's damage (never below 1) and did not touch
 *       the vanilla armor attribute. 5.12 sets it as the armor attribute instead, which the armor
 *       formula turns into a percentage.</li>
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

    /** 4.12 left the vanilla armor attribute alone: undo MythicMobs setting it from the Armor option. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawn(MythicMobSpawnEvent event) {
        resetArmorAttribute(event.getMob());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onStatChange(MythicStatChangeEvent event) {
        if (event.getStat() == Stats.ARMOR && event.getCaster() instanceof ActiveMob mob) resetArmorAttribute(mob);
    }

    private static void resetArmorAttribute(ActiveMob mob) {
        if (!LegacyDamage.enabled(mob, LegacyDamage.Part.MOB_ARMOR) || mob.getType().getArmor(mob) < 0) return;
        if (!(mob.getEntity().getBukkitEntity() instanceof LivingEntity living)) return;
        AttributeInstance armor = living.getAttribute(Attribute.ARMOR);
        if (armor != null) armor.setBaseValue(vanillaArmor(living, armor));
    }

    /**
     * The entity type's own base armor (a zombie has 2). Bukkit only offers the attribute's global default
     * (0), so this asks the server's DefaultAttributes table (Mojang names, Paper runs mojmapped).
     */
    private static double vanillaArmor(LivingEntity entity, AttributeInstance armor) {
        try {
            ClassLoader loader = entity.getClass().getClassLoader();
            Object handle = entity.getClass().getMethod("getHandle").invoke(entity);
            Object type = handle.getClass().getMethod("getType").invoke(handle);
            Class<?> entityType = Class.forName("net.minecraft.world.entity.EntityType", false, loader);
            Object supplier = Class.forName("net.minecraft.world.entity.ai.attributes.DefaultAttributes", false, loader)
                    .getMethod("getSupplier", entityType).invoke(null, type);
            Object holder = Class.forName("net.minecraft.world.entity.ai.attributes.Attributes", false, loader)
                    .getField("ARMOR").get(null);
            Class<?> holderType = Class.forName("net.minecraft.core.Holder", false, loader);
            if (!(Boolean) supplier.getClass().getMethod("hasAttribute", holderType).invoke(supplier, holder)) {
                return armor.getDefaultValue();
            }
            return (Double) supplier.getClass().getMethod("getBaseValue", holderType).invoke(supplier, holder);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return armor.getDefaultValue();
        }
    }

    /** 4.12: {@code damage -= Armor}, at least 1, before the damage modifiers and the armor formula. */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onArmoredDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        ActiveMob mob = MythicBukkit.inst().getMobManager().getMythicMobInstance(living);
        if (mob == null || !LegacyDamage.enabled(mob, LegacyDamage.Part.MOB_ARMOR)) return;
        double armor = mob.getType().getArmor(mob);
        if (armor <= 0 || isAbsoluteSkillDamage(living)) return;
        event.setDamage(Math.max(1.0, event.getDamage() - armor));
    }

    /** {@code damage{ia=true}} set the health directly in 4.12, so Armor never applied to it. */
    private static boolean isAbsoluteSkillDamage(LivingEntity entity) {
        for (MetadataValue value : entity.getMetadata("skill-damage")) {
            if (value.value() instanceof DamageMetadata metadata && Boolean.TRUE.equals(metadata.getIgnoresArmor())) {
                return true;
            }
        }
        return false;
    }

    private record Hit(ActiveMob mob, AbstractEntity damager, TriggeredSkill skill) {
    }
}
