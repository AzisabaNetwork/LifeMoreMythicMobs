package net.azisaba.lifemoremythicmobs.conditions;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.core.skills.SkillCondition;

public class OrCondition extends SkillCondition {
    public OrCondition(MythicLineConfig config) { super(""); }
    public OrCondition(String line, MythicLineConfig config) { super(line); }
}
