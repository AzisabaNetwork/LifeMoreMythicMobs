package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.damage.DamagingMechanic;
import io.lumine.mythic.core.skills.variables.VariableRegistry;
import io.lumine.mythic.core.skills.variables.VariableScope;
import net.azisaba.lifemoremythicmobs.util.ElementalDefenseMath;
import net.azisaba.lifemoremythicmobs.util.HoroloElementalDefenseBridge;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Map;

/** Mob-to-player typed damage with HoroloCore's per-element armor calculation. */
public final class MobTypedDamageMechanic extends DamagingMechanic implements ITargetedEntitySkill {
    private final PlaceholderDouble amount;
    private final boolean configuredIgnoreArmor;
    private final boolean ignoreElementalDefense;

    public MobTypedDamageMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        amount = config.getPlaceholderDouble(new String[]{"amount", "a"}, 1.0D, new String[0]);
        element = config.getPlaceholderString(
                new String[]{"element", "e", "damagetype", "type", "t"}, null, new String[0]);

        configuredIgnoreArmor = ignoresArmor;
        ignoreElementalDefense = config.getBoolean(
                new String[]{"ignoreelement", "ignoreelementaldefense", "iel"}, false);

    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target.isDead()) {
            return SkillResult.INVALID_TARGET;
        }
        if (data.getCaster().isUsingDamageSkill()) {
            return SkillResult.INVALID_TARGET;
        }
        if (target.isLiving() && target.getHealth() <= 0.0D) {
            return SkillResult.INVALID_TARGET;
        }

        double evaluatedAmount = amount.get(data, target);
        double base = evaluatedAmount * (powerAffectsDamage ? data.getPower() : 1.0D);
        String resolvedElement = element == null ? null : element.get(data.getCaster());

        if (resolvedElement != null && !resolvedElement.isEmpty()) {
            Map<String, Double> targetMods = TypeBuffMechanic.getCombinedMods(target.getUniqueId());
            double targetAuraMod = targetMods.getOrDefault(resolvedElement, 1.0D);
            VariableRegistry targetVars = MythicBukkit.inst().getVariableManager()
                    .getRegistry(VariableScope.CASTER, data, target);
            int resLevel = targetVars.getInt("upg_total_" + resolvedElement.toLowerCase() + "_res");
            base *= Math.max(0.0D, targetAuraMod - resLevel * 0.01D);

            Map<String, Double> casterMods = TypeOffensiveBuffMechanic.getCombinedMods(
                    data.getCaster().getEntity().getUniqueId());
            double casterAuraMod = casterMods.getOrDefault(resolvedElement, 1.0D);
            if (data.getCaster().getEntity().getBukkitEntity() instanceof Player player) {
                casterAuraMod += HoroloElementalDefenseBridge.outgoingDamage(player, resolvedElement);
            }
            VariableRegistry casterVars = MythicBukkit.inst().getVariableManager()
                    .getRegistry(VariableScope.CASTER, data, data.getCaster().getEntity());
            int dmgLevel = casterVars.getInt("upg_total_" + resolvedElement.toLowerCase() + "_dmg");
            base *= Math.max(0.0D, casterAuraMod + dmgLevel * 0.01D);
        }

        double vanillaArmor = 0.0D;
        double vanillaToughness = 0.0D;
        LivingEntity living = target.getBukkitEntity() instanceof LivingEntity value ? value : null;
        if (living != null && !configuredIgnoreArmor) {
            vanillaArmor = attributeValue(living, Attribute.ARMOR);
            vanillaToughness = attributeValue(living, Attribute.ARMOR_TOUGHNESS);
        }

        HoroloElementalDefenseBridge.Stats elemental = HoroloElementalDefenseBridge.Stats.ZERO;
        if (!ignoreElementalDefense && living instanceof Player player) {
            elemental = HoroloElementalDefenseBridge.get(player, resolvedElement);
        }

        base = ElementalDefenseMath.prepareForCombatMathPipeline(
                base,
                vanillaArmor,
                vanillaToughness,
                elemental.armor(),
                elemental.toughness(),
                configuredIgnoreArmor,
                ignoreElementalDefense);

        if (!Double.isFinite(base)) {
            base = 0.0D;
        }
        if (resolvedElement != null && !resolvedElement.isEmpty()) {
            data.getVariables().putString("damage-type", resolvedElement);
        }

        doDamage(data, target, base);
        return SkillResult.SUCCESS;
    }

    private static double attributeValue(LivingEntity entity, Attribute attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null || !Double.isFinite(instance.getValue())) {
            return 0.0D;
        }
        return instance.getValue();
    }
}
