package net.azisaba.lifemoremythicmobs.conditions;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.core.skills.SkillCondition;

public class HealthCompareCondition extends SkillCondition {
    public HealthCompareCondition(MythicLineConfig config) { super(""); }
    public HealthCompareCondition(String line, MythicLineConfig config) { super(line); }
}
