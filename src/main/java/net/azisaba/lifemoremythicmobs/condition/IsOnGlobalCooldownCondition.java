package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.conditions.ISkillMetaCondition;
import io.lumine.mythic.core.skills.SkillCondition;
import net.azisaba.lifemoremythicmobs.util.GlobalCooldownManager;

public class IsOnGlobalCooldownCondition extends SkillCondition implements ISkillMetaCondition {

    private final String gcdName;

    public IsOnGlobalCooldownCondition(MythicLineConfig config) {
        super(config.getLine());
        this.gcdName = config.getString(new String[]{"gcdname", "name", "n"}, "default");
    }

    @Override
    public boolean check(SkillMetadata skillMetadata) {
        return GlobalCooldownManager.isOnCooldown(gcdName);
    }
}
