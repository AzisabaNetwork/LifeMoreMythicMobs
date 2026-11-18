package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.skills.variables.VariableRegistry;
import io.lumine.mythic.core.skills.variables.VariableScope;
import net.azisaba.lifemoremythicmobs.mechanic.TypeBuffMechanic;
import net.azisaba.lifemoremythicmobs.mechanic.TypeOffensiveBuffMechanic;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Elements of tDamage / mobTDamage. {@code e=FIRE} is one element as before; {@code e=FIRE,ICE} (or
 * {@code FIRE|ICE}) lists several, and the hit uses the one that deals the most damage to this
 * target: the caster's offensive buffs and upgrades, the target's resist buffs and upgrades, and the
 * target mob's DamageModifiers all count. On a tie the one written first wins, so without any buff
 * the first element is used.
 */
public final class ElementChoice {
    private ElementChoice() {}

    public static List<String> parse(String resolved) {
        List<String> elements = new ArrayList<>();
        if (resolved == null) return elements;
        for (String part : resolved.split("[,|]")) {
            String element = part.trim();
            if (!element.isEmpty()) elements.add(element);
        }
        return elements;
    }

    /** The listed element dealing the most damage, or null when none is listed. */
    public static String choose(List<String> elements, SkillMetadata data, AbstractEntity target) {
        if (elements.isEmpty()) return null;
        if (elements.size() == 1) return elements.get(0);
        String best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (String element : elements) {
            double score = multiplier(element, data, target) * mobDamageModifier(element, target);
            if (score > bestScore) { // strictly greater: the first one wins a tie
                bestScore = score;
                best = element;
            }
        }
        return best;
    }

    /** The LifeMore element multiplier tDamage applies (resist side × offensive side). */
    public static double multiplier(String element, SkillMetadata data, AbstractEntity target) {
        Map<String, Double> targetMods = TypeBuffMechanic.getCombinedMods(target.getUniqueId());
        double targetAuraMod = targetMods.getOrDefault(element, 1.0D);
        VariableRegistry targetVars = MythicBukkit.inst().getVariableManager()
                .getRegistry(VariableScope.CASTER, data, target);
        int resLevel = targetVars.getInt("upg_total_" + element.toLowerCase() + "_res");
        double multiplier = Math.max(0.0D, targetAuraMod - resLevel * 0.01D);

        AbstractEntity caster = data.getCaster().getEntity();
        Map<String, Double> casterMods = TypeOffensiveBuffMechanic.getCombinedMods(caster.getUniqueId());
        double casterAuraMod = casterMods.getOrDefault(element, 1.0D);
        if (caster.getBukkitEntity() instanceof Player player) {
            casterAuraMod += HoroloElementalDefenseBridge.outgoingDamage(player, element);
        }
        VariableRegistry casterVars = MythicBukkit.inst().getVariableManager()
                .getRegistry(VariableScope.CASTER, data, caster);
        int dmgLevel = casterVars.getInt("upg_total_" + element.toLowerCase() + "_dmg");
        return multiplier * Math.max(0.0D, casterAuraMod + dmgLevel * 0.01D);
    }

    private static double mobDamageModifier(String element, AbstractEntity target) {
        ActiveMob mob = MythicBukkit.inst().getMobManager().getActiveMob(target.getUniqueId()).orElse(null);
        if (mob == null || mob.getType().getDamageModifiers() == null) return 1.0D;
        return mob.getType().getDamageModifiers().getOrDefault(element.toUpperCase(), 1.0D);
    }
}
