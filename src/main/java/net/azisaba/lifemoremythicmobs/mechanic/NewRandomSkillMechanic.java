package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.*;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderDouble;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class NewRandomSkillMechanic extends SkillMechanic implements ITargetedEntitySkill {

    private final List<SkillEntry> skillList = new ArrayList<>();

    public NewRandomSkillMechanic(MythicLineConfig config) {
        super(config.getLine(), config);

        String rawSkills = config.getString(new String[]{"skills", "s"}, "");
        for (String entry : rawSkills.split(",")) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                skillList.add(new SkillEntry(parts[0], parts[1]));
            }
        }
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (skillList.isEmpty()) return false;

        double totalWeight = 0;
        double[] resolvedWeights = new double[skillList.size()];

        for (int i = 0; i < skillList.size(); i++) {
            double weight = skillList.get(i).chance.get(data, target);
            resolvedWeights[i] = weight;
            totalWeight += weight;
        }

        if (totalWeight <= 0) return false;

        double randomValue = ThreadLocalRandom.current().nextDouble() * totalWeight;
        double currentWeight = 0;

        for (int i = 0; i < skillList.size(); i++) {
            currentWeight += resolvedWeights[i];
            if (randomValue <= currentWeight) {
                return executeSkill(skillList.get(i).skillName, data);
            }
        }

        return false;
    }

    private boolean executeSkill(String name, SkillMetadata data) {
        Optional<Skill> maybeSkill = MythicMobs.inst().getSkillManager().getSkill(name);
        if (maybeSkill.isPresent()) {
            maybeSkill.get().execute(data.deepClone());
            return true;
        }
        return false;
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