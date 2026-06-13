package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.SkillAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.damage.DamageMetadata;
import io.lumine.xikage.mythicmobs.skills.damage.DamagingMechanic;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderDouble;

import java.util.Map;

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
            Map<String, Double> mods =
                    TypeBuffMechanic.getCombinedMods(target.getUniqueId());

            Double multiplier = mods.get(element);

            if (multiplier != null) {
                base *= multiplier;
            }
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