package net.azisaba.lifemoremythicmobs.conditions;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.core.skills.SkillCondition;

public class NearbyEntityCondition extends SkillCondition {
    public NearbyEntityCondition(MythicLineConfig config) { super(""); }
    public NearbyEntityCondition(String line, MythicLineConfig config) { super(line); }
}
