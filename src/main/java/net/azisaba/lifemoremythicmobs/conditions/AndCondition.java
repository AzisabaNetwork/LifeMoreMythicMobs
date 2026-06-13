package net.azisaba.lifemoremythicmobs.conditions;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.core.skills.SkillCondition;

public class AndCondition extends SkillCondition {
    public AndCondition(MythicLineConfig config) { super(""); }
    public AndCondition(String line, MythicLineConfig config) { super(line); }
}
