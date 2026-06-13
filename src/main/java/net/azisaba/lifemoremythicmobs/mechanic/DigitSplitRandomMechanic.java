package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import net.azisaba.lifemoremythicmobs.util.VariableUtil;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DigitSplitRandomMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final PlaceholderInt max;
   private final List<String> variableNames;

   public DigitSplitRandomMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.max = config.getPlaceholderInteger("max", 999999);
      String rawVars = config.getString("vars", "");
      this.variableNames = Arrays.asList(rawVars.split("\\s*,\\s*"));
      if (this.variableNames.size() != 6) {
         IgaDebugLogger.log(this.getClass(), "'vars' に6つの変数名を指定してください。現在: " + this.variableNames.size());
      }
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      if (this.variableNames.size() != 6) {
         return SkillResult.FAILURE;
      }

      int max = this.max.get(data);
      int random = ThreadLocalRandom.current().nextInt(1, max + 1);
      String padded = String.format("%06d", random);

      for (int i = 0; i < 6; i++) {
         String digit = String.valueOf(padded.charAt(i));
         String scopedVarName = this.variableNames.get(i);
         boolean success = VariableUtil.setScopedVariable(scopedVarName, digit, data, target);
         if (!success) {
            IgaDebugLogger.log(this.getClass(), "変数への保存に失敗: " + scopedVarName);
         }
      }

      return SkillResult.SUCCESS;
   }
}
