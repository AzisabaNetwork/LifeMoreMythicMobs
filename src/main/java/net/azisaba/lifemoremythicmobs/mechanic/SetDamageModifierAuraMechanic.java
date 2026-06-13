package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.listener.DamageAuraManager;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import org.bukkit.entity.LivingEntity;

public class SetDamageModifierAuraMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final PlaceholderString auraName;
   private final PlaceholderDouble multiplier;
   private final PlaceholderInt durationTicks;

   public SetDamageModifierAuraMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.auraName = PlaceholderString.of(config.getString(new String[]{"auraName"}, "default", new String[0]));
      this.multiplier = PlaceholderDouble.of(config.getString(new String[]{"multiplier", "m"}, "1.0", new String[0]));
      this.durationTicks = PlaceholderInt.of(config.getString(new String[]{"duration", "d"}, "100", new String[0]));
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      if (!target.isLiving()) {
         return SkillResult.FAILURE;
      }

      LivingEntity entity = (LivingEntity)target.getBukkitEntity();
      String name = this.auraName.get(data);
      double mult = this.multiplier.get(data);
      long durationMs = this.durationTicks.get(data) * 50L;
      DamageAuraManager.getInstance().applyAura(entity.getUniqueId(), name, mult, durationMs);
      return SkillResult.SUCCESS;
   }
}
