package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SwitchCustomMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final PlaceholderString condition;
   private final boolean uniqueResult;
   private final Map<String, List<String>> caseSkills = new LinkedHashMap<>();
   private List<String> defaultSkills = null;

   public SwitchCustomMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.condition = PlaceholderString.of(config.getString("condition"));
      this.uniqueResult = config.getBoolean("uniqueResult", true);
      String rawCases = config.getString("cases", "");
      String[] rawCaseEntries = rawCases.split("case");
      String[] var7 = rawCaseEntries;
      int var6 = rawCaseEntries.length;

      for (int var5 = 0; var5 < var6; var5++) {
         String raw = var7[var5];
         raw = raw.trim();
         if (!raw.isEmpty() && raw.contains("-")) {
            String[] split = raw.split("=", 2);
            String key = split[0].trim().toUpperCase();
            String skillBlock = split[1].trim();
            if (skillBlock.startsWith("[") && skillBlock.endsWith("]")) {
               skillBlock = skillBlock.substring(1, skillBlock.length() - 1);
            }

            String[] lines = skillBlock.split(";");
            List<String> skills = new ArrayList<>();
            String[] var16 = lines;
            int var15 = lines.length;

            for (int var14 = 0; var14 < var15; var14++) {
               String line = var16[var14];
               line = line.trim();
               if (!line.isEmpty()) {
                  skills.add(line);
               }
            }

            if (key.equals("DEFAULT")) {
               this.defaultSkills = skills;
            } else {
               this.caseSkills.put(key, skills);
            }
         }
      }
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      String value = this.condition.get(data).toUpperCase();
      boolean executed = false;
      List<String> skillLines = this.caseSkills.getOrDefault(value, null);
      if (skillLines != null) {
         for (String line : skillLines) {
            Optional<Skill> opt = MythicBukkit.inst().getSkillManager().getSkill(line);
            if (opt.isPresent()) {
               Skill skill = opt.get();
               data.setTrigger(target);
               skill.execute(data);
               executed = true;
               if (this.uniqueResult) {
                  return SkillResult.SUCCESS;
               }
            }
         }
      }

      if (!executed && this.defaultSkills != null) {
         for (String line : this.defaultSkills) {
            Optional<Skill> opt = MythicBukkit.inst().getSkillManager().getSkill(line);
            if (opt.isPresent()) {
               Skill skill = opt.get();
               data.setTrigger(target);
               skill.execute(data);
               executed = true;
            }
         }
      }

      return executed;
   }
}
