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
import io.lumine.mythic.core.skills.variables.Variable;
import io.lumine.mythic.core.skills.variables.VariableType;

import java.util.Optional;

public class CallWithArgsMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private final String skillName;
    private final PlaceholderString argsString;

    public CallWithArgsMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.skillName = config.getString(new String[]{"skill", "s"});
        this.argsString = PlaceholderString.of(config.getString(new String[]{"args", "a"}));
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        Optional<Skill> maybeSkill = MythicBukkit.inst().getSkillManager().getSkill(skillName);
        if (!maybeSkill.isPresent()) return SkillResult.CONDITION_FAILED;

        SkillMetadata newData = data.deepClone();
        String resolvedArgs = argsString.get(data, target);

        if (resolvedArgs != null && !resolvedArgs.isEmpty()) {
            String[] pairs = resolvedArgs.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    String key = kv[0].trim();
                    String value = kv[1].trim();
                    newData.getVariables().put(key, Variable.ofType(VariableType.STRING, value));
                }
            }
        }
        maybeSkill.get().execute(newData);
        return SkillResult.SUCCESS;
    }
}