package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.logging.MythicLogger;
import io.lumine.mythic.logging.MythicLogger.DebugLevel;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.AbstractSkill.ThreadSafetyLevel;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

public class PotionClearCustomMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final PlaceholderString effectPlaceholder;

   public PotionClearCustomMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY;
      String effect = config.getString(new String[]{"effects", "effect", "type", "t"});
      this.effectPlaceholder = new PlaceholderString(effect != null ? effect : "");
   }

   public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
      LivingEntity livingEntity = (LivingEntity)abstractEntity.getBukkitEntity();
      String evaluated = this.effectPlaceholder.get(skillMetadata, abstractEntity);
      String[] potionNames = evaluated.split(",");
      boolean success = false;
      String[] var10 = potionNames;
      int var9 = potionNames.length;

      for (int var8 = 0; var8 < var9; var8++) {
         String potion = var10[var8];
         potion = potion.trim().toUpperCase();
         PotionEffectType type = PotionEffectType.getByName(potion);
         if (type != null) {
            livingEntity.removePotionEffect(type);
            success = true;
         } else {
            MythicLogger.debug(DebugLevel.MECHANIC, "Invalid potion type: " + potion, new Object[0]);
         }
      }

      return success;
   }
}
