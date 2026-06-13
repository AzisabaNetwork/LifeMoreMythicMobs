package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;

/**
 * Stub for DamageAsMechanic.
 * Original source preserved in _original_iga/ directory.
 * TODO: Adapt to MM 5.12 API from the decompiled original.
 */
public class DamageAsMechanic extends SkillMechanic {
    public DamageAsMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
    }
}
