package net.azisaba.lifemoremythicmobs.conditions;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.core.skills.SkillCondition;

public class WorldNotInConfigCondition extends SkillCondition {
    public WorldNotInConfigCondition(MythicLineConfig config) { super(""); }
    public WorldNotInConfigCondition(String line, MythicLineConfig config) { super(line); }
}
