package net.azisaba.lifemoremythicmobs.conditions;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;

public class NotHasAuraCondition extends SkillCondition implements IEntityCondition {
   private final String auraName;

   public NotHasAuraCondition(MythicLineConfig config) {
      super(config.getLine());
      this.auraName = config.getString(
         new String[]{"name", "aura", "auraname", "buffname", "buff", "debuffname", "debuff", "n", "b"}, this.conditionVar, new String[0]
      );
   }

   public boolean check(AbstractEntity target) {
      return target != null && !this.auraName.isEmpty()
         ? !getPlugin().getSkillManager().getAuraManager().getAuraRegistry(target).hasAura(this.auraName)
         : false;
   }
}
