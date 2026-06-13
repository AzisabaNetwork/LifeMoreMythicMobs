package net.azisaba.lifemoremythicmobs.conditions;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.core.skills.SkillCondition;

public class NotHasAuraCondition extends SkillCondition {
    public NotHasAuraCondition(MythicLineConfig config) { super(""); }
    public NotHasAuraCondition(String line, MythicLineConfig config) { super(line); }
}
