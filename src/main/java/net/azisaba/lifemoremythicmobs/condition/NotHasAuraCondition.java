package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.core.skills.SkillCondition;

import java.util.List;

public class NotHasAuraCondition extends SkillCondition implements IEntityCondition {
   private final List<String> auraNames;

   public NotHasAuraCondition(MythicLineConfig config) {
      super(config.getLine());
      this.auraNames = ConditionMatchUtil.parseCommaSeparated(config.getString(
         new String[]{"name", "aura", "auraname", "buffname", "buff", "debuffname", "debuff", "n", "b"}, this.getConditionVar(), new String[0]
      ));
   }

   public boolean check(AbstractEntity target) {
      if (target == null || this.auraNames.isEmpty()) {
         return false;
      }
      var registry = getPlugin().getSkillManager().getAuraManager().getAuraRegistry(target);
      for (String auraName : this.auraNames) {
         if (registry.hasAura(auraName)) {
            return false;
         }
      }
      return true;
   }
}



