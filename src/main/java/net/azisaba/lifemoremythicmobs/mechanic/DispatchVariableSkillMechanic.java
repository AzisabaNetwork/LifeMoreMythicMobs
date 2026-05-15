package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.Skill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString;
import org.bukkit.Bukkit;

import java.util.Optional;

public class DispatchVariableSkillMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private final PlaceholderString pattern;

    public DispatchVariableSkillMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.pattern = PlaceholderString.of(config.getString(new String[]{"pattern", "p"}));
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        String resolvedSkillName = pattern.get(data, target);

        if (resolvedSkillName == null || resolvedSkillName.isEmpty()) {
            return false;
        }

        Optional<Skill> maybeSkill = MythicMobs.inst().getSkillManager().getSkill(resolvedSkillName);

        if (maybeSkill.isPresent()) {
            maybeSkill.get().execute(data);
            return true;
        } else {
            Bukkit.getLogger().warning("[Lmmm] Dispatcher: スキル '" + resolvedSkillName + "' が見つかりませんでした。");
            return false;
        }
    }
}