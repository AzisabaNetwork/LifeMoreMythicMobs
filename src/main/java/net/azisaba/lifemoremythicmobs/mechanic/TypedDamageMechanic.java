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
import net.azisaba.lifemoremythicmobs.util.HoroloElementalDefenseBridge;
import org.bukkit.entity.Player;

import java.util.Map;

public class TypedDamageMechanic extends DamagingMechanic implements ITargetedEntitySkill {

    protected final PlaceholderDouble amount;
    /** The element(s) as written: one element, or several separated by "," or "|". */
    protected final io.lumine.mythic.api.skills.placeholders.PlaceholderString elementSpec;

    public TypedDamageMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);

        this.amount = config.getPlaceholderDouble(new String[]{"amount", "a"}, 1.0, new String[0]);
        // Keep igaMM's type/t aliases while using MM 5.12's placeholder-aware element field.
        this.element = config.getPlaceholderString(
                new String[]{"element", "e", "damagetype", "type", "t"}, null, new String[0]
        );
        this.elementSpec = this.element;
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
        // e=FIRE,ICE: several elements, the hit takes the one dealing the most damage (see ElementChoice)
        String resolvedElement = net.azisaba.lifemoremythicmobs.util.ElementChoice.choose(
                net.azisaba.lifemoremythicmobs.util.ElementChoice.parse(
                        this.elementSpec == null ? null : this.elementSpec.get(data.getCaster())),
                data, target);

        if (resolvedElement != null && !resolvedElement.isEmpty()) {
            // 被ダメ側 (Aura + Upgrade) × 与ダメ側 (Aura + Upgrade)
            base *= net.azisaba.lifemoremythicmobs.util.ElementChoice.multiplier(resolvedElement, data, target);
        }

        // Preserve every finite 1.15.2 result, but never pass NaN/Infinity into MM/Bukkit.
        if (!Double.isFinite(base)) {
            base = 0.0D;
        }

        if (resolvedElement != null && !resolvedElement.isEmpty()) {
            data.getVariables().putString("damage-type", resolvedElement);
        }

        // Delegate metadata construction and all MM damage flags to 5.12 itself. MM reads the element
        // (for DamageModifiers and tags) from this.element, so hand it the chosen one for this hit only.
        io.lumine.mythic.api.skills.placeholders.PlaceholderString previous = this.element;
        this.element = resolvedElement == null ? null
                : io.lumine.mythic.api.skills.placeholders.PlaceholderString.of(resolvedElement);
        try {
            net.azisaba.lifemoremythicmobs.util.DamageCompat.doDamage(this, data, target, base);
        } finally {
            this.element = previous;
        }

        // 時計塔 8 層の共鳴反応: プレイヤーの属性ダメージなら、どの Mob でも cl8_reso_<属性> を唱えさせる
        net.azisaba.lifemoremythicmobs.util.ResonanceHook.afterTypedDamage(data.getCaster().getEntity(), target, resolvedElement);

        return SkillResult.SUCCESS;
    }
}



