package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.util.VariableUtil;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;

public class VarReplaceRegexCustomMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final PlaceholderString regexVariable;
   private final PlaceholderString replacementVariable;
   private final PlaceholderString from;
   private final String to;

   public VarReplaceRegexCustomMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.regexVariable = PlaceholderString.of(config.getString(new String[]{"regexVariable", "regex", "r", "正規表現"}));
      this.replacementVariable = PlaceholderString.of(config.getString(new String[]{"replacementVariable", "replacement", "rep", "置換"}));
      this.from = PlaceholderString.of(config.getString(new String[]{"from"}));
      this.to = config.getString(new String[]{"to"});
   }

   public boolean castAtEntity(SkillMetadata data, AbstractEntity entity) {
      String regex = this.regexVariable.get(data);
      String replacement = this.replacementVariable.get(data);
      String from = this.from.get(data);
      if (from != null && regex != null && replacement != null) {
         VariableUtil.setScopedVariable(this.to, from.replaceAll(regex, replacement), data, entity);
         return SkillResult.SUCCESS;
      } else {
         return SkillResult.FAILURE;
      }
   }
}
