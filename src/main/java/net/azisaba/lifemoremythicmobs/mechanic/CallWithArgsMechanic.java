package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.Skill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString;
import io.lumine.xikage.mythicmobs.skills.variables.Variable;
import io.lumine.xikage.mythicmobs.skills.variables.VariableType;

import java.util.Optional;

public class CallWithArgsMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private final String skillName;
    private final PlaceholderString argsString;

    public CallWithArgsMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.skillName = config.getString(new String[]{"skill", "s"});
        this.argsString = PlaceholderString.of(config.getString(new String[]{"args", "a"}));
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        Optional<Skill> maybeSkill = MythicMobs.inst().getSkillManager().getSkill(skillName);
        if (!maybeSkill.isPresent()) return false;

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
        return true;
    }
}