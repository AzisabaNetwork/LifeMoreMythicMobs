package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import org.bukkit.Bukkit;

import java.util.Optional;

public class DispatchVariableSkillMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private final PlaceholderString pattern;

    public DispatchVariableSkillMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.pattern = PlaceholderString.of(config.getString(new String[]{"pattern", "p"}));
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        String resolvedSkillName = pattern.get(data, target);

        if (resolvedSkillName == null || resolvedSkillName.isEmpty()) {
            return SkillResult.CONDITION_FAILED;
        }

        Optional<Skill> maybeSkill = MythicBukkit.inst().getSkillManager().getSkill(resolvedSkillName);

        if (maybeSkill.isPresent()) {
            maybeSkill.get().execute(data);
            return SkillResult.SUCCESS;
        } else {
            Bukkit.getLogger().warning("[Lmmm] Dispatcher: スキル '" + resolvedSkillName + "' が見つかりませんでした。");
            return SkillResult.CONDITION_FAILED;
        }
    }
}