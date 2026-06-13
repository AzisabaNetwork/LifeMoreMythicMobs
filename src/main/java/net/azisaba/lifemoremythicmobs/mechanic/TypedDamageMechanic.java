package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.SkillAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.damage.DamageMetadata;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
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

        double base = amount.get(data.getCaster()) * data.getPower();

        if (!element.isEmpty()) {
            Map<String, Double> mods =
                    TypeBuffMechanic.getCombinedMods(target.getUniqueId());

            Double multiplier = mods.get(element);

            if (multiplier != null) {
                base *= multiplier;
            }
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
