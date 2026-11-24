package net.azisaba.lifemoremythicmobs.listener;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.skills.variables.VariableManager;
import io.lumine.mythic.core.skills.variables.VariableScope;
import net.azisaba.lifemoremythicmobs.mechanic.TypeBuffMechanic;
import net.azisaba.lifemoremythicmobs.mechanic.TypeOffensiveBuffMechanic;
import net.azisaba.lifemoremythicmobs.util.ElementalDefenseMath;
import net.azisaba.lifemoremythicmobs.util.HoroloElementalDefenseBridge;
import net.azisaba.lifemoremythicmobs.util.MeleeElement;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Colours a direct melee hit (ENTITY_ATTACK or sweep) with the attacker's {@code MeleeElement} (item or mob
 * definition), so it is treated like elemental damage:
 * <ul>
 *   <li>the same element multipliers as tDamage (typeBuff, typeOffensiveBuff, upgrades, HoroloCore orbs);</li>
 *   <li>a MythicMobs target takes its DamageModifiers entry for the element instead of ENTITY_ATTACK
 *       (LegacyDamage ModifierOrder mobs through {@link LegacyMobDamageListener}, others by the ratio
 *       element / ENTITY_ATTACK, because MythicMobs itself always applies the ENTITY_ATTACK entry to melee);</li>
 *   <li>a player target takes the HoroloCore elemental defense for the element.</li>
 * </ul>
 */
public final class MeleeElementListener implements Listener {
    private static final Map<EntityDamageEvent, String> ELEMENTS = new IdentityHashMap<>();

    /** The element given to this melee hit, or null (used by the legacy DamageModifiers order). */
    static String elementOf(EntityDamageEvent event) {
        return ELEMENTS.get(event);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMelee(EntityDamageByEntityEvent event) {
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK
                && cause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) return;
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (target.hasMetadata("skill-damage")) return; // skill damage (damage / tDamage) has its own element
        String element = MeleeElement.of(attacker);
        if (element == null) return;
        ELEMENTS.put(event, element);

        double damage = event.getDamage() * multiplier(element, attacker, target);
        ActiveMob mob = MythicBukkit.inst().getMobManager().getMythicMobInstance(target);
        if (mob != null && !LegacyDamage.enabled(mob, LegacyDamage.Part.MODIFIER_ORDER)) {
            Map<String, Double> mods = mob.getType().getDamageModifiers();
            if (mods != null) {
                double el = mods.getOrDefault(element.toUpperCase(), 1.0D);
                double ea = mods.getOrDefault(cause.name(), 1.0D);
                if (ea != 0.0D) damage *= el / ea; // MythicMobs multiplies by the ENTITY_ATTACK entry afterwards
            }
        }
        if (target instanceof Player player) {
            HoroloElementalDefenseBridge.Stats def = HoroloElementalDefenseBridge.get(player, element);
            damage = ElementalDefenseMath.prepareForCombatMathPipeline(damage,
                    attribute(player, Attribute.ARMOR), attribute(player, Attribute.ARMOR_TOUGHNESS),
                    def.armor(), def.toughness(), false, false);
        }
        event.setDamage(Double.isFinite(damage) ? Math.max(0.0D, damage) : 0.0D);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void cleanup(EntityDamageEvent event) {
        ELEMENTS.remove(event);
    }

    /** The element multiplier of tDamage, from the entities' own variables (no skill involved). */
    private static double multiplier(String element, LivingEntity attacker, LivingEntity target) {
        AbstractEntity a = BukkitAdapter.adapt(attacker);
        AbstractEntity t = BukkitAdapter.adapt(target);
        VariableManager vars = MythicBukkit.inst().getVariableManager();
        double targetMod = TypeBuffMechanic.getCombinedMods(t.getUniqueId()).getOrDefault(element, 1.0D);
        int res = vars.getRegistry(VariableScope.CASTER, t).getInt("upg_total_" + element.toLowerCase() + "_res");
        double m = Math.max(0.0D, targetMod - res * 0.01D);
        double attackMod = TypeOffensiveBuffMechanic.getCombinedMods(a.getUniqueId()).getOrDefault(element, 1.0D);
        if (attacker instanceof Player player) attackMod += HoroloElementalDefenseBridge.outgoingDamage(player, element);
        int dmg = vars.getRegistry(VariableScope.CASTER, a).getInt("upg_total_" + element.toLowerCase() + "_dmg");
        return m * Math.max(0.0D, attackMod + dmg * 0.01D);
    }

    private static double attribute(LivingEntity entity, Attribute attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance == null || !Double.isFinite(instance.getValue()) ? 0.0D : instance.getValue();
    }
}
