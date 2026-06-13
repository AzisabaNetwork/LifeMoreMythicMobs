package net.azisaba.lifemoremythicmobs.conditions;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.core.skills.SkillCondition;

public class NotCondition extends SkillCondition {
    public NotCondition(MythicLineConfig config) { super(""); }
    public NotCondition(String line, MythicLineConfig config) { super(line); }
}
