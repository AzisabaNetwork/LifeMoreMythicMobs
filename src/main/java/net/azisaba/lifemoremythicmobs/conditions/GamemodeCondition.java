package net.azisaba.lifemoremythicmobs.conditions;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.core.skills.SkillCondition;

public class GamemodeCondition extends SkillCondition {
    public GamemodeCondition(MythicLineConfig config) { super(""); }
    public GamemodeCondition(String line, MythicLineConfig config) { super(line); }
}
