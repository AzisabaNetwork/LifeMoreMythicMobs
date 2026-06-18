package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.SkillAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.damage.DamageMetadata;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.core.skills.variables.VariableRegistry;
import io.lumine.mythic.core.skills.variables.VariableScope;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.damage.DamagingMechanic;

import java.util.Map;

public class TypedDamageMechanic extends DamagingMechanic implements ITargetedEntitySkill {

    protected final PlaceholderDouble amount;
    protected final String element;
    protected final boolean ignoresArmor;
    protected final boolean preventsImmunity;
    protected final boolean preventsKnockback;

    public TypedDamageMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);

        this.amount = PlaceholderDouble.of(
                config.getString(new String[]{"amount", "a"}, "1")
        );
        this.element = config.getString(new String[]{"element", "e", "type", "t"}, "");
        this.ignoresArmor = config.getBoolean(new String[]{"ignorearmor", "ia", "i"}, false);
        this.preventsImmunity = config.getBoolean(new String[]{"preventimmunity", "pi"}, false);
        this.preventsKnockback = config.getBoolean(new String[]{"preventknockback", "pkb", "pk"}, false);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {

        if (target.isDead()) {
            return SkillResult.CONDITION_FAILED;
        }

        if (data.getCaster().isUsingDamageSkill()) {
            return SkillResult.CONDITION_FAILED;
        }

        if (target.isLiving() && target.getHealth() <= 0.0D) {
            return SkillResult.CONDITION_FAILED;
        }

        double evaluatedAmount = amount.get(data, target);
        double power = data.getPower();
        double base = evaluatedAmount * power;

        double multiplier = 1.0;
        double targetAuraMod = 1.0;
        int resLevel = 0;
        double upgradeRes = 0.0;
        double casterAuraMod = 1.0;
        int dmgLevel = 0;
        double upgradeDmg = 0.0;

        if (!element.isEmpty()) {

            // 被ダメ側の補正 (Aura + Upgrade)
            Map<String, Double> targetMods = TypeBuffMechanic.getCombinedMods(target.getUniqueId());
            targetAuraMod = targetMods.getOrDefault(element, 1.0);

            VariableRegistry targetVars = MythicBukkit.inst().getVariableManager().getRegistry(VariableScope.CASTER, data, target);
            resLevel = targetVars.getInt("upg_total_" + element.toLowerCase() + "_res");
            upgradeRes = resLevel * 0.01;

            multiplier *= Math.max(0, targetAuraMod - upgradeRes);

            // 与ダメ側の補正 (Aura + Upgrade)
            Map<String, Double> casterMods = TypeOffensiveBuffMechanic.getCombinedMods(data.getCaster().getEntity().getUniqueId());
            casterAuraMod = casterMods.getOrDefault(element, 1.0);

            VariableRegistry casterVars = MythicBukkit.inst().getVariableManager().getRegistry(VariableScope.CASTER, data, data.getCaster().getEntity());
            dmgLevel = casterVars.getInt("upg_total_" + element.toLowerCase() + "_dmg");
            upgradeDmg = dmgLevel * 0.01;

            multiplier *= Math.max(0, casterAuraMod + upgradeDmg);

            base *= multiplier;
        }

        DamageMetadata meta = new DamageMetadata(
                data.getCaster(),
                data,
                null,
                base,
                null,
                null,
                element.isEmpty() ? null : element,
                0,
                ignoresArmor,
                preventsImmunity,
                preventsKnockback,
                false,
                null
        );

        if (!element.isEmpty()) {
            data.getVariables().putString("damage-type", element);
        }

        try {
            data.getCaster().setUsingDamageSkill(true);
            SkillAdapter.get().doDamage(meta, target);
        } finally {
            data.getCaster().setUsingDamageSkill(false);
        }

        return SkillResult.SUCCESS;
    }
}
