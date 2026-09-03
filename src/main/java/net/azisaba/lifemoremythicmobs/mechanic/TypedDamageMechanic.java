package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.core.skills.variables.VariableRegistry;
import io.lumine.mythic.core.skills.variables.VariableScope;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.damage.DamagingMechanic;

import java.util.Map;

public class TypedDamageMechanic extends DamagingMechanic implements ITargetedEntitySkill {

    protected final PlaceholderDouble amount;

    public TypedDamageMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);

        this.amount = config.getPlaceholderDouble(new String[]{"amount", "a"}, 1.0, new String[0]);
        // Keep igaMM's type/t aliases while using MM 5.12's placeholder-aware element field.
        this.element = config.getPlaceholderString(
                new String[]{"element", "e", "damagetype", "type", "t"}, null, new String[0]
        );
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
        double base = evaluatedAmount * (this.powerAffectsDamage ? data.getPower() : 1.0);
        String resolvedElement = this.element == null ? null : this.element.get(data.getCaster());

        double multiplier = 1.0;
        double targetAuraMod = 1.0;
        int resLevel = 0;
        double upgradeRes = 0.0;
        double casterAuraMod = 1.0;
        int dmgLevel = 0;
        double upgradeDmg = 0.0;

        if (resolvedElement != null && !resolvedElement.isEmpty()) {

            // 被ダメ側の補正 (Aura + Upgrade)
            Map<String, Double> targetMods = TypeBuffMechanic.getCombinedMods(target.getUniqueId());
            targetAuraMod = targetMods.getOrDefault(resolvedElement, 1.0);

            VariableRegistry targetVars = MythicBukkit.inst().getVariableManager().getRegistry(VariableScope.CASTER, data, target);
            resLevel = targetVars.getInt("upg_total_" + resolvedElement.toLowerCase() + "_res");
            upgradeRes = resLevel * 0.01;

            multiplier *= Math.max(0, targetAuraMod - upgradeRes);

            // 与ダメ側の補正 (Aura + Upgrade)
            Map<String, Double> casterMods = TypeOffensiveBuffMechanic.getCombinedMods(data.getCaster().getEntity().getUniqueId());
            casterAuraMod = casterMods.getOrDefault(resolvedElement, 1.0);

            VariableRegistry casterVars = MythicBukkit.inst().getVariableManager().getRegistry(VariableScope.CASTER, data, data.getCaster().getEntity());
            dmgLevel = casterVars.getInt("upg_total_" + resolvedElement.toLowerCase() + "_dmg");
            upgradeDmg = dmgLevel * 0.01;

            multiplier *= Math.max(0, casterAuraMod + upgradeDmg);

            base *= multiplier;
        }

        if (resolvedElement != null && !resolvedElement.isEmpty()) {
            data.getVariables().putString("damage-type", resolvedElement);
        }

        // Delegate metadata construction and all MM damage flags to 5.12 itself.
        this.doDamage(data, target, base);

        return SkillResult.SUCCESS;
    }
}



