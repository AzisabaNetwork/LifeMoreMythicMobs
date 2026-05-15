package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class NewRandomSkillMechanic extends SkillMechanic implements ITargetedEntitySkill {

    private final List<SkillEntry> skillList = new ArrayList<>();

    public NewRandomSkillMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);

        String rawSkills = config.getString(new String[]{"skills", "s"}, "");
        for (String entry : rawSkills.split(",")) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                skillList.add(new SkillEntry(parts[0], parts[1]));
            }
        }
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (skillList.isEmpty()) return SkillResult.CONDITION_FAILED;

        double totalWeight = 0;
        double[] resolvedWeights = new double[skillList.size()];

        for (int i = 0; i < skillList.size(); i++) {
            double weight = skillList.get(i).chance.get(data, target);
            resolvedWeights[i] = weight;
            totalWeight += weight;
        }

        if (totalWeight <= 0) return SkillResult.CONDITION_FAILED;

        double randomValue = ThreadLocalRandom.current().nextDouble() * totalWeight;
        double currentWeight = 0;

        for (int i = 0; i < skillList.size(); i++) {
            currentWeight += resolvedWeights[i];
            if (randomValue <= currentWeight) {
                return executeSkill(skillList.get(i).skillName, data);
            }
        }

        return SkillResult.CONDITION_FAILED;
    }

    private SkillResult executeSkill(String name, SkillMetadata data) {
        Optional<Skill> maybeSkill = MythicBukkit.inst().getSkillManager().getSkill(name);
        if (maybeSkill.isPresent()) {
            maybeSkill.get().execute(data.deepClone());
            return SkillResult.SUCCESS;
        }
        return SkillResult.CONDITION_FAILED;
    }

    private static class SkillEntry {
        protected final String skillName;
        protected final PlaceholderDouble chance;

        public SkillEntry(String name, String chanceStr) {
            this.skillName = name;
            this.chance = PlaceholderDouble.of(chanceStr);
        }
    }
}