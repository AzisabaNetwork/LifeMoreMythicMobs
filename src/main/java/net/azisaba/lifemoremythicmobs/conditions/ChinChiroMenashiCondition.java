package net.azisaba.lifemoremythicmobs.conditions;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.logging.MythicLogger;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.api.skills.conditions.ILocationCondition;
import io.lumine.mythic.api.skills.conditions.ISkillMetaCondition;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.Arrays;
import java.util.Optional;
import org.bukkit.Bukkit;

public class ChinChiroMenashiCondition extends SkillCondition implements ISkillMetaCondition, IEntityCondition, ILocationCondition {
   private PlaceholderInt d1;
   private PlaceholderInt d2;
   private PlaceholderInt d3;
   private final boolean invert;
   private PlaceholderString action;

   public ChinChiroMenashiCondition(String line, MythicLineConfig config) {
      super(line);
      Bukkit.getLogger().info("[ChinchiroMenashiCondition] lineの中身:" + line);
      this.invert = config.getBoolean("invert", false);
      this.action = PlaceholderString.of(config.getString(new String[]{"action"}, "", new String[0]));

      try {
         this.d1 = PlaceholderInt.of(config.getString(new String[]{"d1"}, "1", new String[0]));
      } catch (Exception var5) {
         MythicLogger.errorConditionConfig(this, config, "Variable name must be set.");
         return;
      }

      try {
         this.d2 = PlaceholderInt.of(config.getString(new String[]{"d2"}, "1", new String[0]));
      } catch (Exception var5) {
         MythicLogger.errorConditionConfig(this, config, "Variable name must be set.");
         return;
      }

      try {
         this.d3 = PlaceholderInt.of(config.getString(new String[]{"d3"}, "1", new String[0]));
      } catch (Exception var5) {
         MythicLogger.errorConditionConfig(this, config, "Variable name must be set.");
      }
   }

   private boolean evaluate(int d1, int d2, int d3) {
      String logPrefix = "[ChinchiroMenashiCondition]";
      int[] dice = new int[]{d1, d2, d3};
      Arrays.sort(dice);
      int a = dice[0];
      int b = dice[1];
      int c = dice[2];
      if (a == 1 && b == 1 && c == 1) {
         return false;
      } else if (a == b && b == c) {
         return false;
      } else if (Arrays.equals(dice, new int[]{4, 5, 6})) {
         return false;
      } else {
         return Arrays.equals(dice, new int[]{1, 2, 3}) ? false : a != b && b != c;
      }
   }

   private boolean applyInvert(boolean result) {
      return this.invert ? !result : result;
   }

   public boolean check(SkillMetadata meta) {
      boolean result = this.applyInvert(
         this.evaluate(
            this.d1.get(meta, meta.getCaster().getEntity()), this.d2.get(meta, meta.getCaster().getEntity()), this.d3.get(meta, meta.getCaster().getEntity())
         )
      );
      if (!result && this.action != null) {
         String skillName = this.action.get(meta);
         Optional<Skill> maybeSkill = MythicBukkit.inst().getSkillManager().getSkill(skillName);
         if (maybeSkill.isPresent() && maybeSkill.get().isUsable(meta)) {
            maybeSkill.get().execute(meta);
         }
      }

      return result;
   }

   public boolean check(AbstractEntity entity) {
      return this.applyInvert(this.evaluate(this.d1.get(entity), this.d2.get(entity), this.d3.get(entity)));
   }

   public boolean check(AbstractLocation location) {
      return this.applyInvert(this.evaluate(this.d1.get(), this.d2.get(), this.d3.get()));
   }
}
