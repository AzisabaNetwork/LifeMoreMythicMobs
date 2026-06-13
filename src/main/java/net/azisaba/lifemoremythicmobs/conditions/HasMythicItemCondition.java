package net.azisaba.lifemoremythicmobs.conditions;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.core.skills.SkillCondition;

public class HasMythicItemCondition extends SkillCondition {
    public HasMythicItemCondition(MythicLineConfig config) { super(""); }
    public HasMythicItemCondition(String line, MythicLineConfig config) { super(line); }
}
