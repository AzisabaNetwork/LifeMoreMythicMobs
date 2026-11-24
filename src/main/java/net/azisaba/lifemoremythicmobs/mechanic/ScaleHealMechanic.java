package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.skills.triggers.meta.HealedMetadata;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * {@code scaleHeal{m=1.5;reasons=REGEN,MAGIC_REGEN}} in an {@code ~onHealed} skill: multiplies this heal.
 * {@code reasons} limits it to those Bukkit RegainReason names (empty = every reason).
 */
public class ScaleHealMechanic extends SkillMechanic implements INoTargetSkill {
    private final PlaceholderDouble multiplier;
    private final Set<String> reasons = new HashSet<>();

    public ScaleHealMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, null, config.getLine(), config);
        this.multiplier = config.getPlaceholderDouble(new String[]{"multiplier", "m", "amount", "a"}, 1.0D);
        String raw = config.getString(new String[]{"reasons", "reason", "r"}, "");
        for (String s : raw.split(",")) {
            if (!s.isBlank()) reasons.add(s.trim().toUpperCase(Locale.ROOT));
        }
    }

    @Override
    public SkillResult cast(SkillMetadata data) {
        if (data.isAsync() || !(data.getTriggerMetadata() instanceof HealedMetadata meta)) {
            return SkillResult.INVALID_CONFIG;
        }
        EntityRegainHealthEvent event = meta.getEvent();
        if (!reasons.isEmpty() && !reasons.contains(event.getRegainReason().name())) {
            return SkillResult.CONDITION_FAILED;
        }
        double amount = event.getAmount() * multiplier.get(data);
        if (!Double.isFinite(amount) || amount < 0) {
            return SkillResult.INVALID_CONFIG;
        }
        event.setAmount(amount);
        meta.setAmount(amount);
        return SkillResult.SUCCESS;
    }
}
