package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.auras.Aura;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class HeightAuraMechanic extends Aura {
   private String onHeightIncreaseSkillName;
   private Map<UUID, Double> previousYMap = new HashMap<>();

   public HeightAuraMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.onHeightIncreaseSkillName = config.getString("onheightincreaseskill", "");
   }

   public void auraStart(AbstractEntity target, SkillMetadata data) {
      if (target != null) {
         this.previousYMap.put(target.getUniqueId(), target.getLocation().getY());
      }
   }

   public void auraTick(AbstractEntity target, SkillMetadata data) {
      if (target != null) {
         double currentY = target.getLocation().getY();
         double previousY = this.previousYMap.getOrDefault(target.getUniqueId(), currentY);
         if (currentY > previousY && this.onHeightIncreaseSkillName != null && !this.onHeightIncreaseSkillName.isEmpty()) {
            Optional<Skill> skill = MythicBukkit.inst().getSkillManager().getSkill(this.onHeightIncreaseSkillName);
            if (skill.isPresent()) {
               SkillMetadata cloned = data.deepClone();
               cloned.setEntityTarget(target);
               skill.get().execute(cloned);
            }
         }

         this.previousYMap.put(target.getUniqueId(), currentY);
      }
   }

   public void auraEnd(AbstractEntity target, SkillMetadata data) {
      this.previousYMap.remove(target.getUniqueId());
   }
}
