package net.azisaba.lifemoremythicmobs.conditions;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.core.skills.SkillCondition;

public class CuboidCustomCondition extends SkillCondition {
    public CuboidCustomCondition(MythicLineConfig config) { super(""); }
    public CuboidCustomCondition(String line, MythicLineConfig config) { super(line); }
}
