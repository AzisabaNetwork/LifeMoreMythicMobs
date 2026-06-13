package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.SkillAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.damage.DamageMetadata;
import io.lumine.xikage.mythicmobs.skills.damage.DamagingMechanic;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderDouble;
import io.lumine.xikage.mythicmobs.skills.variables.VariableRegistry;
import io.lumine.xikage.mythicmobs.skills.variables.VariableScope;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;

import java.util.Map;
import java.util.Optional;

public class TypedDamageMechanic extends DamagingMechanic implements ITargetedEntitySkill {

    protected final PlaceholderDouble amount;
    protected final String element;
    protected final boolean ignoresArmor;
    protected final boolean preventsImmunity;
    protected final boolean preventsKnockback;

    public TypedDamageMechanic(MythicLineConfig config) {
        super(config.getLine(), config);

        this.amount = PlaceholderDouble.of(
                config.getString(new String[]{"amount", "a"}, "1")
        );
        this.element = config.getString(new String[]{"element", "e", "type", "t"}, "");
        this.ignoresArmor = config.getBoolean(new String[]{"ignorearmor", "ia", "i"}, false);
        this.preventsImmunity = config.getBoolean(new String[]{"preventimmunity", "pi"}, false);
        this.preventsKnockback = config.getBoolean(new String[]{"preventknockback", "pkb", "pk"}, false);
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {

        if (target.isDead()) {
            return false;
        }

        if (data.getCaster().isUsingDamageSkill()) {
            return false;
        }

        if (target.isLiving() && target.getHealth() <= 0.0D) {
            return false;
        }

        double base = amount.get(data, target) * data.getPower();

        if (!element.isEmpty()) {
            double multiplier = 1.0;

            // 被ダメ側の補正 (Aura + Upgrade)
            Map<String, Double> targetMods = TypeBuffMechanic.getCombinedMods(target.getUniqueId());
            double targetAuraMod = targetMods.getOrDefault(element, 1.0);

            VariableRegistry targetVars = MythicMobs.inst().getVariableManager().getRegistry(VariableScope.CASTER, data, target);
            int resLevel = targetVars.getInt("upg_total_" + element.toLowerCase() + "_res");
            double upgradeRes = resLevel * 0.01;

            multiplier *= Math.max(0, targetAuraMod - upgradeRes);

            // MythicMobs の DamageModifiers を反映
            Optional<ActiveMob> am = MythicMobs.inst().getMobManager().getActiveMob(target.getUniqueId());
            if (am.isPresent()) {
                Map<String, Double> modifiers = am.get().getType().getDamageModifiers();
                if (modifiers.containsKey(element.toUpperCase())) {
                    multiplier *= modifiers.get(element.toUpperCase());
                }
            }

            // 与ダメ側の補正 (Aura + Upgrade)
            Map<String, Double> casterMods = TypeOffensiveBuffMechanic.getCombinedMods(data.getCaster().getEntity().getUniqueId());
            double casterAuraMod = casterMods.getOrDefault(element, 1.0);

            VariableRegistry casterVars = MythicMobs.inst().getVariableManager().getRegistry(VariableScope.CASTER, data, data.getCaster().getEntity());
            int dmgLevel = casterVars.getInt("upg_total_" + element.toLowerCase() + "_dmg");
            double upgradeDmg = dmgLevel * 0.01;

            multiplier *= Math.max(0, casterAuraMod + upgradeDmg);

            base *= multiplier;
        }

        DamageMetadata meta = new DamageMetadata(
                data.getCaster(),
                base,
                element.isEmpty() ? null : element,
                ignoresArmor,
                preventsImmunity,
                preventsKnockback
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

        return true;
    }
}