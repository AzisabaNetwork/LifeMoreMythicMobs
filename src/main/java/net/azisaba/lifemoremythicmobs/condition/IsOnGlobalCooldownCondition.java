package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.conditions.ISkillMetaCondition;
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
