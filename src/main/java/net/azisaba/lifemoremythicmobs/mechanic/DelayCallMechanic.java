package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillManager;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import java.util.Optional;
import org.bukkit.Bukkit;

public class DelayCallMechanic extends SkillMechanic implements INoTargetSkill {
   private final PlaceholderDouble ticks;
   private final String skillName;

   public DelayCallMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.ticks = PlaceholderDouble.of(config.getString(new String[]{"t", "time", "ticks"}, "0", new String[0]));
      this.skillName = config.getString(new String[]{"s", "skill"}, null, new String[0]);
   }

   public SkillResult cast(SkillMetadata data) {
      if (this.skillName != null && !this.skillName.trim().isEmpty()) {
         double v = this.ticks.get(data);
         long ticks = Math.max(0L, Math.round(Double.isFinite(v) ? v : 0.0));
         SkillManager sm = MythicBukkit.inst().getSkillManager();
         Optional<Skill> opt = sm.getSkill(this.skillName);
         if (!opt.isPresent()) {
            return SkillResult.FAILURE;
         }

         Skill toRun = opt.get();
         Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), () -> {
            try {
               toRun.execute(data.deepClone());
            } catch (Throwable var3) {
            }
         }, ticks);
         return SkillResult.SUCCESS;
      } else {
         return SkillResult.FAILURE;
      }
   }
}
