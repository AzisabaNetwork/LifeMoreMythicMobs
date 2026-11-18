package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.conditions.ISkillMetaCondition;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.skills.SkillCondition;

/**
 * Checks if the trigger (or caster target) is currently the top threat holder of the caster ActiveMob.
 * Usage: ?lmIsTopThreat
 */
public class TopThreatCondition extends SkillCondition implements ISkillMetaCondition {
    public TopThreatCondition(MythicLineConfig config) {
        super(config.getLine());
    }

    @Override
    public boolean check(SkillMetadata data) {
        if (data == null || !(data.getCaster() instanceof ActiveMob mob) || !mob.hasThreatTable()) {
            return false;
        }
        AbstractEntity top = mob.getThreatTable().getTopThreatHolder();
        if (top == null) return false;
        AbstractEntity checkTarget = data.getTrigger() != null ? data.getTrigger() : mob.getEntity().getTarget();
        return checkTarget != null && top.getUniqueId().equals(checkTarget.getUniqueId());
    }
}
